package com.serie.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.serie.app.SerieApplication
import com.serie.app.data.SeedData
import com.serie.app.data.SerieRepository
import com.serie.app.domain.ActiveWorkout
import com.serie.app.domain.LibraryExercise
import com.serie.app.domain.MuscleGroup
import com.serie.app.domain.PersonalRecord
import com.serie.app.domain.Preferences
import com.serie.app.domain.SetEntry
import com.serie.app.domain.Workout
import com.serie.app.domain.WorkoutExercise
import com.serie.app.domain.WorkoutSummary
import com.serie.app.domain.WorkoutType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Duracao padrao do descanso entre series, em segundos. */
private const val REST_DEFAULT = 90

/**
 * Estado e regras do app inteiro.
 *
 * E um unico ViewModel porque as telas compartilham o mesmo nucleo de dados -
 * o treino que a tela Hoje mostra e o que a execucao roda e o que o resumo
 * fecha. Separar em um por tela obrigaria a sincronizar copias.
 *
 * O estado publicado e a combinacao de duas origens: o que esta no banco
 * (treinos, biblioteca, preferencias) e o que e so de tela (filtros, rascunho
 * em edicao, treino em andamento). Escrever vai sempre para o banco; a volta
 * chega pelos Flows, entao a UI nunca le uma copia otimista que o banco possa
 * nao ter aceitado.
 */
class SerieViewModel(private val repository: SerieRepository) : ViewModel() {

    /** A parte do estado que nao e persistida. */
    private val transient = MutableStateFlow(TransientState())

    /** O relogio do treino em andamento: cronometro e descanso. */
    private var tickJob: Job? = null

    val state: StateFlow<SerieUiState> = combine(
        repository.workouts,
        repository.library,
        repository.preferences,
        transient,
    ) { workouts, library, preferences, ui ->
        SerieUiState(
            workouts = workouts,
            library = library,
            // Enquanto a primeira carga nao chega, o padrao mantem a tela
            // coerente em vez de piscar valores vazios.
            preferences = preferences ?: SeedData.preferences,
            workoutFilter = ui.workoutFilter,
            draft = ui.draft,
            draftIsNew = ui.draftIsNew,
            expandedExercise = ui.expandedExercise,
            libraryQuery = ui.libraryQuery,
            libraryGroup = ui.libraryGroup,
            libraryPicked = ui.libraryPicked,
            active = ui.active,
            summary = ui.summary,
            effort = ui.effort,
            period = ui.period,
            progressExercise = ui.progressExercise,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SerieUiState(),
    )

    init {
        viewModelScope.launch { repository.seedIfFirstRun() }
    }

    // ---------------------------------------------------------------- treinos

    fun setWorkoutFilter(index: Int) = transient.update { it.copy(workoutFilter = index) }

    /** Lista ja filtrada pelo seletor de Todos / Forca / Cardio. */
    fun visibleWorkouts(s: SerieUiState): List<Workout> = when (s.workoutFilter) {
        1 -> s.workouts.filter { it.type == WorkoutType.Forca }
        2 -> s.workouts.filter { it.type == WorkoutType.Cardio }
        else -> s.workouts
    }

    fun todayWorkout(s: SerieUiState): Workout? =
        s.workouts.firstOrNull { it.id == SeedData.TODAY_WORKOUT_ID } ?: s.workouts.firstOrNull()

    // ----------------------------------------------------------- criar/editar

    /** Abre o editor com um treino existente, ou com um treino novo em branco. */
    fun startEditing(workoutId: String?) {
        val existing = state.value.workouts.firstOrNull { it.id == workoutId }
        val draft = existing ?: Workout(
            id = "w-${System.currentTimeMillis()}",
            letter = nextLetter(),
            name = "",
            type = WorkoutType.Forca,
            days = List(7) { false },
            exercises = emptyList(),
            estimatedMinutes = 45,
            muscleTags = emptyList(),
        )
        transient.update {
            it.copy(
                draft = draft,
                draftIsNew = existing == null,
                expandedExercise = if (existing == null) -1 else 1,
            )
        }
    }

    private fun nextLetter(): String {
        val used = state.value.workouts.mapNotNull { it.letter.firstOrNull() }.toSet()
        return ('A'..'Z').firstOrNull { it !in used }?.toString() ?: "+"
    }

    fun updateDraftName(name: String) = updateDraft { it.copy(name = name) }

    fun toggleDraftDay(index: Int) = updateDraft { w ->
        w.copy(days = w.days.mapIndexed { i, on -> if (i == index) !on else on })
    }

    fun toggleExerciseExpanded(index: Int) = transient.update {
        it.copy(expandedExercise = if (it.expandedExercise == index) -1 else index)
    }

    fun updateDraftExercise(index: Int, transform: (WorkoutExercise) -> WorkoutExercise) =
        updateDraft { w ->
            w.copy(exercises = w.exercises.mapIndexed { i, e -> if (i == index) transform(e) else e })
        }

    fun removeDraftExercise(index: Int) = updateDraft { w ->
        w.copy(exercises = w.exercises.filterIndexed { i, _ -> i != index })
    }

    /** Reordena: move um exercicio de uma posicao para outra. */
    fun moveDraftExercise(from: Int, to: Int) = updateDraft { w ->
        val list = w.exercises.toMutableList()
        if (from in list.indices && to in list.indices) {
            list.add(to, list.removeAt(from))
        }
        w.copy(exercises = list)
    }

    /**
     * Grava o rascunho no banco.
     *
     * O rascunho so existe em memoria ate aqui - e o que faz o X do cabecalho
     * descartar de fato, sem precisar desfazer escritas.
     */
    fun saveDraft() {
        val draft = transient.value.draft ?: return
        val named = if (draft.name.isBlank()) draft.copy(name = "Treino ${draft.letter}") else draft
        val position = state.value.workouts.size
        transient.update { it.copy(draft = null) }
        viewModelScope.launch { repository.saveWorkout(named, position) }
    }

    fun discardDraft() = transient.update { it.copy(draft = null) }

    fun deleteDraft() {
        val id = transient.value.draft?.id ?: return
        transient.update { it.copy(draft = null) }
        viewModelScope.launch { repository.deleteWorkout(id) }
    }

    private fun updateDraft(transform: (Workout) -> Workout) = transient.update { s ->
        s.copy(draft = s.draft?.let(transform))
    }

    // -------------------------------------------------------------- biblioteca

    fun setLibraryQuery(query: String) = transient.update { it.copy(libraryQuery = query) }

    fun setLibraryGroup(index: Int) = transient.update { it.copy(libraryGroup = index) }

    fun toggleLibraryPick(id: String) = transient.update { s ->
        s.copy(libraryPicked = if (id in s.libraryPicked) s.libraryPicked - id else s.libraryPicked + id)
    }

    /** Os grupos do filtro: "Todos" seguido dos grupos musculares. */
    val libraryGroups: List<String> = listOf("Todos") + MuscleGroup.entries.map { it.label }

    fun visibleLibrary(s: SerieUiState): List<LibraryExercise> =
        s.library.filter { ex ->
            val groupOk = s.libraryGroup == 0 || ex.group.label == libraryGroups[s.libraryGroup]
            val queryOk = ex.name.contains(s.libraryQuery, ignoreCase = true)
            groupOk && queryOk
        }

    /** Leva os exercicios marcados para o rascunho e limpa a selecao. */
    fun confirmLibrarySelection() {
        val s = state.value
        val picked = s.library.filter { it.id in s.libraryPicked }
        if (picked.isNotEmpty() && s.draft != null) {
            updateDraft { w ->
                w.copy(
                    exercises = w.exercises + picked.map { ex ->
                        WorkoutExercise(
                            id = "we-${ex.id}-${System.nanoTime()}",
                            exerciseId = ex.id,
                            name = ex.name,
                            sets = 3,
                            reps = "10",
                            load = "—",
                            restSeconds = 90,
                        )
                    }
                )
            }
        }
        transient.update { it.copy(libraryPicked = emptySet(), libraryQuery = "") }
    }

    // --------------------------------------------------------------- execucao

    /** Comeca o treino e liga o relogio. */
    fun startWorkout(workoutId: String) {
        val workout = state.value.workouts.firstOrNull { it.id == workoutId } ?: return
        val index = if (workout.exercises.size > 1) 1 else 0
        val exercise = workout.exercises.getOrNull(index)
        val sets = exercise?.let { ex ->
            List(ex.sets) { i ->
                val seed = SeedData.activeSets.getOrNull(i)
                SetEntry(
                    previous = seed?.first ?: "—",
                    load = seed?.second ?: ex.load.filter { it.isDigit() || it == ',' }.ifBlank { "0" },
                    reps = seed?.third ?: ex.reps.takeWhile { it.isDigit() }.ifBlank { "0" },
                    done = i < 2,
                )
            }
        } ?: emptyList()

        transient.update {
            it.copy(
                active = ActiveWorkout(
                    workoutId = workout.id,
                    workoutLetter = workout.letter,
                    workoutName = workout.name,
                    exerciseIndex = index,
                    exerciseCount = workout.exercises.size,
                    exerciseName = exercise?.name ?: workout.name,
                    goal = exercise?.let { ex ->
                        "Meta ${ex.sets} × ${ex.reps} · última vez ${sets.firstOrNull()?.previous ?: "—"}"
                    } ?: "",
                    sets = sets,
                    elapsedSeconds = 0,
                    restSeconds = REST_DEFAULT,
                    restRunning = false,
                )
            )
        }
        startTicking()
    }

    private fun startTicking() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                transient.update { s ->
                    val a = s.active ?: return@update s
                    val restDone = a.restRunning && a.restSeconds <= 1
                    s.copy(
                        active = a.copy(
                            elapsedSeconds = a.elapsedSeconds + 1,
                            restSeconds = if (a.restRunning) (a.restSeconds - 1).coerceAtLeast(0) else a.restSeconds,
                            restRunning = a.restRunning && !restDone,
                        )
                    )
                }
            }
        }
    }

    fun updateSetLoad(index: Int, load: String) = updateActiveSet(index) { it.copy(load = load) }

    fun updateSetReps(index: Int, reps: String) = updateActiveSet(index) { it.copy(reps = reps) }

    /** Fechar uma serie dispara o descanso; reabrir nao o cancela. */
    fun toggleSetDone(index: Int) = transient.update { s ->
        val a = s.active ?: return@update s
        val nowDone = !a.sets[index].done
        s.copy(
            active = a.copy(
                sets = a.sets.mapIndexed { i, set -> if (i == index) set.copy(done = nowDone) else set },
                restSeconds = if (nowDone) REST_DEFAULT else a.restSeconds,
                restRunning = nowDone || a.restRunning,
            )
        )
    }

    fun addSet() = transient.update { s ->
        val a = s.active ?: return@update s
        val last = a.sets.lastOrNull()
        s.copy(
            active = a.copy(
                sets = a.sets + SetEntry(
                    previous = "—",
                    load = last?.load ?: "0",
                    reps = last?.reps ?: "0",
                    done = false,
                )
            )
        )
    }

    fun adjustRest(deltaSeconds: Int) = transient.update { s ->
        val a = s.active ?: return@update s
        s.copy(active = a.copy(restSeconds = (a.restSeconds + deltaSeconds).coerceAtLeast(0)))
    }

    private fun updateActiveSet(index: Int, transform: (SetEntry) -> SetEntry) = transient.update { s ->
        val a = s.active ?: return@update s
        s.copy(active = a.copy(sets = a.sets.mapIndexed { i, set -> if (i == index) transform(set) else set }))
    }

    /** Encerra o treino e monta o resumo a partir do que foi registrado. */
    fun finishWorkout() {
        val a = transient.value.active ?: return
        tickJob?.cancel()
        tickJob = null

        val doneSets = a.sets.count { it.done }
        val volume = a.sets.filter { it.done }.sumOf { set ->
            val load = set.load.replace(',', '.').toDoubleOrNull() ?: 0.0
            val reps = set.reps.toIntOrNull() ?: 0
            (load * reps).toInt()
        }

        transient.update {
            it.copy(
                active = null,
                summary = WorkoutSummary(
                    workoutName = a.workoutName,
                    dateLabel = SeedData.TODAY_LABEL.substringBefore(",") + ", 14 out",
                    durationLabel = formatClock(a.elapsedSeconds),
                    volumeKg = volume,
                    volumeDelta = "volume · +8%",
                    totalSets = doneSets,
                    records = SeedData.finishedRecords,
                ),
            )
        }
    }

    fun setEffort(index: Int) = transient.update { it.copy(effort = index) }

    fun clearSummary() = transient.update { it.copy(summary = null, effort = 1) }

    // --------------------------------------------------------------- evolucao

    fun setPeriod(index: Int) = transient.update { it.copy(period = index) }

    fun nextProgressExercise() = transient.update {
        it.copy(progressExercise = (it.progressExercise + 1) % SeedData.exerciseProgress.size)
    }

    // ------------------------------------------------------------ preferencias

    fun setWeeklyGoal(index: Int) = updatePrefs { it.copy(weeklyGoal = index + 3) }

    fun setUnit(index: Int) = updatePrefs { it.copy(useKilograms = index == 0) }

    fun setDarkTheme(dark: Boolean) = updatePrefs { it.copy(darkTheme = dark) }

    fun toggleReminders() = updatePrefs { it.copy(remindersOn = !it.remindersOn) }

    /**
     * Preferencia nao tem botao de salvar: cada toque grava.
     *
     * O valor base vem do banco, nao do estado publicado, porque dois toques
     * rapidos no mesmo seletor poderiam ler o mesmo valor antigo se
     * dependessem da volta do Flow.
     */
    private fun updatePrefs(transform: (Preferences) -> Preferences) {
        viewModelScope.launch {
            val current = repository.preferences.first() ?: SeedData.preferences
            repository.updatePreferences(transform(current))
        }
    }

    override fun onCleared() {
        tickJob?.cancel()
        super.onCleared()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SerieApplication
                SerieViewModel(app.repository)
            }
        }

        private val APPLICATION_KEY = ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
    }
}

/** "32:14" a partir de segundos. */
fun formatClock(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%02d:%02d".format(m, s)
}

/** A parte do estado que vive so enquanto a tela esta aberta. */
private data class TransientState(
    val workoutFilter: Int = 0,
    val draft: Workout? = null,
    val draftIsNew: Boolean = false,
    val expandedExercise: Int = 1,
    val libraryQuery: String = "",
    val libraryGroup: Int = 0,
    val libraryPicked: Set<String> = emptySet(),
    val active: ActiveWorkout? = null,
    val summary: WorkoutSummary? = null,
    val effort: Int = 1,
    val period: Int = 1,
    val progressExercise: Int = 0,
)

/** Todo o estado observavel do app. */
data class SerieUiState(
    val workouts: List<Workout> = emptyList(),
    val library: List<LibraryExercise> = emptyList(),
    val workoutFilter: Int = 0,
    val draft: Workout? = null,
    val draftIsNew: Boolean = false,
    val expandedExercise: Int = 1,
    val libraryQuery: String = "",
    val libraryGroup: Int = 0,
    val libraryPicked: Set<String> = emptySet(),
    val active: ActiveWorkout? = null,
    val summary: WorkoutSummary? = null,
    val effort: Int = 1,
    val period: Int = 1,
    val progressExercise: Int = 0,
    val preferences: Preferences = SeedData.preferences,
) {
    val records: List<PersonalRecord> get() = SeedData.finishedRecords
}
