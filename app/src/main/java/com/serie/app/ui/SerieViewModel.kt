package com.serie.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.serie.app.SerieApplication
import com.serie.app.data.SeedData
import com.serie.app.data.SerieRepository
import com.serie.app.domain.ActiveSession
import com.serie.app.domain.BodyWeight
import com.serie.app.domain.ActiveWorkout
import com.serie.app.domain.CompletedSession
import com.serie.app.domain.CompletedSet
import com.serie.app.domain.DateFormat
import com.serie.app.domain.EvolutionPeriod
import com.serie.app.domain.ExerciseProgress
import com.serie.app.domain.MuscleVolume
import com.serie.app.domain.LibraryExercise
import com.serie.app.domain.MuscleGroup
import com.serie.app.domain.PersonalRecord
import com.serie.app.domain.Preferences
import com.serie.app.domain.SetEntry
import com.serie.app.domain.Stats
import com.serie.app.domain.UserProfile
import com.serie.app.domain.Workout
import com.serie.app.domain.WorkoutExercise
import com.serie.app.domain.WorkoutSummary
import com.serie.app.domain.WeekDay
import com.serie.app.domain.WorkoutType
import com.serie.app.domain.toView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Duracao padrao do descanso entre series, em segundos. */
private const val REST_DEFAULT = 90

/**
 * Estado e regras do app inteiro.
 *
 * E um unico ViewModel porque as telas compartilham o mesmo nucleo de dados -
 * o treino que a tela Hoje mostra e o que a execucao roda e o que o resumo
 * fecha. Separar em um por tela obrigaria a sincronizar copias.
 *
 * O estado publicado combina o que esta no banco (treinos, biblioteca,
 * preferencias, treino em andamento) com o que e so de tela (filtros,
 * rascunho em edicao, dialogos). Escrever vai sempre para o banco; a volta
 * chega pelos Flows, entao a UI nunca le uma copia otimista.
 */
class SerieViewModel(private val repository: SerieRepository) : ViewModel() {

    /** A parte do estado que nao e persistida. */
    private val transient = MutableStateFlow(TransientState())

    // Seis fontes nao cabem num combine so (as sobrecargas vao ate cinco),
    // entao as sessoes entram por um segundo bloco.
    private val sessionData = combine(
        repository.activeSession,
        repository.history,
        repository.bodyWeights,
    ) { active, history, weights -> Triple(active, history, weights) }

    private val persisted = combine(
        repository.workouts,
        repository.library,
        repository.preferences,
        repository.profile,
        sessionData,
    ) { workouts, library, preferences, profile, sessions ->
        PersistedState(
            workouts = workouts,
            library = library,
            preferences = preferences ?: SeedData.preferences,
            session = sessions.first,
            profile = profile,
            history = sessions.second,
            bodyWeights = sessions.third,
        )
    }

    val state: StateFlow<SerieUiState> = combine(persisted, transient) { db, ui ->
        val today = LocalDate.now()
        val history = db.history
        SerieUiState(
            today = today,
            todayLabel = DateFormat.longDate(today),
            history = history,
            week = Stats.week(history, db.workouts, today),
            todaySessions = Stats.sessionsOn(history, today),
            bodyWeights = db.bodyWeights,
            recordingWeight = ui.recordingWeight,
            streak = Stats.streakDays(history, today),
            weekVolume = Stats.formatVolume(Stats.weekVolumeKg(history, today)),
            sessionsThisWeek = Stats.sessionsThisWeek(history, today),
            monthDelta = Stats.monthDelta(history, today),
            lastRecord = Stats.lastRecord(history),
            totalWorkouts = Stats.totalWorkouts(history),
            totalRecords = Stats.totalRecords(history),
            volumeChart = Stats.volumeChart(history, ui.period, today),
            muscleVolumes = Stats.muscleVolumes(history),
            exerciseProgressList = Stats.exerciseProgress(history),
            workouts = db.workouts,
            library = db.library,
            preferences = db.preferences,
            profile = db.profile,
            editingProfile = ui.editingProfile,
            // A sessao guardada so vira numero de cronometro aqui, resolvida
            // contra o tique atual.
            active = db.session?.toView(ui.nowMillis),
            workoutFilter = ui.workoutFilter,
            draft = ui.draft,
            draftIsNew = ui.draftIsNew,
            expandedExercise = ui.expandedExercise,
            libraryQuery = ui.libraryQuery,
            libraryGroup = ui.libraryGroup,
            libraryPicked = ui.libraryPicked,
            exerciseDraft = ui.exerciseDraft,
            exerciseDraftIsNew = ui.exerciseDraftIsNew,
            exerciseError = ui.exerciseError,
            sessionToDelete = ui.sessionToDelete,
            discardingWorkout = ui.discardingWorkout,
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
        startClock()
    }

    /**
     * O relogio so corre enquanto existe treino em andamento.
     *
     * Ele nao decrementa nada: apenas publica o instante atual, e a sessao
     * resolve cronometro e descanso contra ele. Por isso o tempo continua
     * correto mesmo que o app tenha passado meia hora morto.
     */
    private fun startClock() {
        viewModelScope.launch {
            repository.activeSession
                .map { it != null }
                .distinctUntilChanged()
                .collectLatest { hasSession ->
                    while (hasSession && isActive) {
                        transient.update { it.copy(nowMillis = System.currentTimeMillis()) }
                        delay(1000)
                    }
                }
        }
    }

    // ---------------------------------------------------------------- treinos

    fun setWorkoutFilter(index: Int) = transient.update { it.copy(workoutFilter = index) }

    /** Lista ja filtrada pelo seletor de Todos / Forca / Cardio. */
    fun visibleWorkouts(s: SerieUiState): List<Workout> = when (s.workoutFilter) {
        1 -> s.workouts.filter { it.type == WorkoutType.Forca }
        2 -> s.workouts.filter { it.type == WorkoutType.Cardio }
        else -> s.workouts
    }

    /**
     * O treino agendado para hoje, pelo dia da semana.
     *
     * Antes vinha de um id fixo do prototipo, com queda para o primeiro da
     * lista quando ele nao existia - o que fazia o cartao anunciar um treino
     * que nao e de hoje, enquanto a faixa da semana dizia outra coisa. Sem
     * nada agendado, nao ha treino do dia: a tela mostra descanso.
     */
    fun todayWorkout(s: SerieUiState): Workout? = scheduledOn(s.workouts, s.today)

    /** O treino marcado para um dia da semana, se houver. */
    fun scheduledOn(workouts: List<Workout>, date: java.time.LocalDate): Workout? {
        val dayIndex = date.dayOfWeek.value - 1
        return workouts.firstOrNull { it.days.getOrNull(dayIndex) == true }
    }

    /** Este treino esta agendado para hoje? */
    fun isScheduledToday(s: SerieUiState, workout: Workout): Boolean =
        workout.days.getOrNull(s.today.dayOfWeek.value - 1) == true

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

    fun openCreateExercise() = transient.update {
        it.copy(
            exerciseDraft = LibraryExercise(
                id = "",
                name = "",
                group = MuscleGroup.Peito,
                equipment = "",
                isCustom = true,
            ),
            exerciseDraftIsNew = true,
            exerciseError = null,
        )
    }

    fun openEditExercise(exercise: LibraryExercise) = transient.update {
        it.copy(exerciseDraft = exercise, exerciseDraftIsNew = false, exerciseError = null)
    }

    fun dismissExerciseDialog() =
        transient.update { it.copy(exerciseDraft = null, exerciseError = null) }

    /**
     * Grava o rascunho do exercicio.
     *
     * Um exercicio novo ja sai marcado: quem o cria no meio da montagem de um
     * treino quer usa-lo agora, e obriga-lo a procurar na lista depois seria
     * um passo a toa.
     */
    fun saveExercise(name: String, group: MuscleGroup, equipment: String, notes: String) {
        val draft = transient.value.exerciseDraft ?: return
        val isNew = transient.value.exerciseDraftIsNew
        viewModelScope.launch {
            if (isNew) {
                val created = repository.createExercise(name, group, equipment, notes)
                if (created == null) {
                    transient.update { it.copy(exerciseError = nameError(name)) }
                } else {
                    transient.update {
                        it.copy(
                            exerciseDraft = null,
                            exerciseError = null,
                            libraryQuery = "",
                            libraryGroup = 0,
                            libraryPicked = it.libraryPicked + created.id,
                        )
                    }
                }
            } else {
                val ok = repository.updateExercise(
                    draft.copy(name = name, group = group, equipment = equipment, notes = notes)
                )
                if (ok) {
                    transient.update { it.copy(exerciseDraft = null, exerciseError = null) }
                } else {
                    transient.update { it.copy(exerciseError = nameError(name)) }
                }
            }
        }
    }

    private fun nameError(name: String): String =
        if (name.isBlank()) "Dê um nome ao exercício."
        else "Já existe um exercício com esse nome."

    fun deleteCustomExercise(id: String) {
        transient.update { it.copy(libraryPicked = it.libraryPicked - id) }
        viewModelScope.launch { repository.deleteCustomExercise(id) }
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

    /**
     * Monta as series de um exercicio a partir da prescricao e do historico.
     *
     * "Anterior" vem do ultimo treino que registrou este exercicio; a carga e
     * as reps partem dali, e so caem na prescricao quando nao ha historico.
     */
    private suspend fun buildSetsFor(
        workout: Workout,
        index: Int,
    ): List<SetEntry> {
        val exercise = workout.exercises.getOrNull(index) ?: return emptyList()
        val lastSets = repository.history.first()
            .filter { session -> session.sets.any { it.exerciseId == exercise.exerciseId } }
            .maxByOrNull { it.startedAtMillis }
            ?.sets
            ?.filter { it.exerciseId == exercise.exerciseId }
            .orEmpty()

        val defaultLoad = exercise.load.filter { it.isDigit() || it == ',' }.ifBlank { "0" }
        val defaultReps = exercise.reps.takeWhile { it.isDigit() }.ifBlank { "0" }
        return List(exercise.sets) { i ->
            val previous = lastSets.getOrNull(i)
            SetEntry(
                exerciseIndex = index,
                exerciseId = exercise.exerciseId,
                previous = previous
                    ?.let { Stats.formatKg(it.loadKg) + " × " + it.reps }
                    ?: "—",
                load = previous?.let { Stats.formatKg(it.loadKg).removeSuffix(" kg") }
                    ?: defaultLoad,
                reps = previous?.reps?.toString() ?: defaultReps,
                done = false,
            )
        }
    }

    private fun goalOf(workout: Workout, index: Int, sets: List<SetEntry>): String {
        val exercise = workout.exercises.getOrNull(index) ?: return ""
        val last = sets.firstOrNull()?.previous ?: "—"
        return "Meta ${exercise.sets} × ${exercise.reps} · última vez $last"
    }

    /** O descanso do exercicio em foco, ou o padrao quando nao ha prescricao. */
    private fun restSecondsOf(workoutId: String, index: Int): Int =
        state.value.workouts
            .firstOrNull { it.id == workoutId }
            ?.exercises?.getOrNull(index)
            ?.restSeconds
            ?.takeIf { it > 0 }
            ?: REST_DEFAULT

    /**
     * Comeca o treino - ou retoma o que ja estava em andamento.
     *
     * Retomar e o caso comum depois do app ser morto no bolso: recomecar do
     * zero aqui apagaria as series que o usuario ja tinha registrado.
     */
    fun startWorkout(workoutId: String) {
        viewModelScope.launch {
            val existing = repository.activeSession.first()
            if (existing != null) return@launch

            val workout = state.value.workouts.firstOrNull { it.id == workoutId } ?: return@launch
            val index = 0
            val exercise = workout.exercises.getOrNull(index)
            val sets = buildSetsFor(workout, index)

            repository.saveActiveSession(
                ActiveSession(
                    workoutId = workout.id,
                    workoutLetter = workout.letter,
                    workoutName = workout.name,
                    exerciseIndex = index,
                    exerciseCount = workout.exercises.size,
                    exerciseId = exercise?.exerciseId.orEmpty(),
                    exerciseName = exercise?.name ?: workout.name,
                    goal = goalOf(workout, index, sets),
                    sets = sets,
                    startedAtMillis = System.currentTimeMillis(),
                    restEndsAtMillis = null,
                    restRemainingSeconds = exercise?.restSeconds?.takeIf { it > 0 } ?: REST_DEFAULT,
                )
            )
        }
    }

    /**
     * Troca o exercicio em foco.
     *
     * As series do exercicio anterior continuam guardadas: o usuario pode
     * voltar e completar o que deixou pela metade, e tudo entra no historico
     * ao finalizar.
     */
    fun goToExercise(index: Int) {
        viewModelScope.launch {
            val session = repository.activeSession.first() ?: return@launch
            if (index == session.exerciseIndex) return@launch
            if (index !in 0 until session.exerciseCount) return@launch

            val workout = state.value.workouts.firstOrNull { it.id == session.workoutId }
                ?: return@launch
            val exercise = workout.exercises.getOrNull(index) ?: return@launch

            val sets = if (session.hasSetsFor(index)) {
                session.sets
            } else {
                session.sets + buildSetsFor(workout, index)
            }
            val visible = sets.filter { it.exerciseIndex == index }

            repository.saveActiveSession(
                session.copy(
                    exerciseIndex = index,
                    exerciseId = exercise.exerciseId,
                    exerciseName = exercise.name,
                    goal = goalOf(workout, index, visible),
                    sets = sets,
                    // O descanso nao atravessa a troca: o tempo do exercicio
                    // anterior nao diz nada sobre o proximo.
                    restEndsAtMillis = null,
                    restRemainingSeconds = exercise.restSeconds.takeIf { it > 0 } ?: REST_DEFAULT,
                )
            )
        }
    }

    fun nextExercise() {
        val session = state.value.active ?: return
        goToExercise(session.exerciseIndex + 1)
    }

    fun previousExercise() {
        val session = state.value.active ?: return
        goToExercise(session.exerciseIndex - 1)
    }

    /** Descarta o treino em andamento sem gravar nada no historico. */
    fun askDiscardWorkout() = transient.update { it.copy(discardingWorkout = true) }

    fun dismissDiscardWorkout() = transient.update { it.copy(discardingWorkout = false) }

    fun confirmDiscardWorkout() {
        transient.update { it.copy(discardingWorkout = false) }
        viewModelScope.launch { repository.clearActiveSession() }
    }

    fun updateSetLoad(index: Int, load: String) = updateSet(index) { it.copy(load = load) }

    fun updateSetReps(index: Int, reps: String) = updateSet(index) { it.copy(reps = reps) }

    /**
     * Fechar uma serie dispara o descanso do proprio exercicio.
     *
     * O valor vem da prescricao a cada toque, e nao de uma copia feita ao
     * iniciar: assim editar o descanso no treino passa a valer na hora.
     */
    fun toggleSetDone(index: Int) = withSession { s ->
        val global = s.globalIndexOf(index)
        if (global < 0) return@withSession s
        val nowDone = !s.sets[global].done
        val rest = restSecondsOf(s.workoutId, s.exerciseIndex)
        s.copy(
            sets = s.sets.mapIndexed { i, set ->
                if (i == global) set.copy(done = nowDone) else set
            },
            restEndsAtMillis = if (nowDone) {
                System.currentTimeMillis() + rest * 1000L
            } else {
                s.restEndsAtMillis
            },
            restRemainingSeconds = if (nowDone) rest else s.restRemainingSeconds,
        )
    }

    fun addSet() = withSession { s ->
        val last = s.currentSets().lastOrNull()
        s.copy(
            sets = s.sets + SetEntry(
                exerciseIndex = s.exerciseIndex,
                exerciseId = s.exerciseId,
                previous = "—",
                load = last?.load ?: "0",
                reps = last?.reps ?: "0",
                done = false,
            )
        )
    }

    /** Move o fim do descanso; com ele parado, ajusta o que sobrou. */
    fun adjustRest(deltaSeconds: Int) = withSession { s ->
        val now = System.currentTimeMillis()
        val ends = s.restEndsAtMillis
        if (ends != null && ends > now) {
            // So o fim se move. restRemainingSeconds guarda a duracao
            // configurada do exercicio e e o que a barra mostra quando esta
            // parada - mexer nele deixaria "00:01" no lugar do tempo cheio
            // depois de drenar o descanso com -15s.
            s.copy(restEndsAtMillis = (ends + deltaSeconds * 1000L).coerceAtLeast(now))
        } else {
            // Parado, o ajuste muda a duracao do proximo descanso. O piso de
            // 15 s evita chegar a um "00:00" que nao e um descanso.
            s.copy(
                restRemainingSeconds =
                    (s.restRemainingSeconds + deltaSeconds).coerceAtLeast(15)
            )
        }
    }

    private fun updateSet(index: Int, transform: (SetEntry) -> SetEntry) = withSession { s ->
        val global = s.globalIndexOf(index)
        if (global < 0) return@withSession s
        s.copy(sets = s.sets.mapIndexed { i, set -> if (i == global) transform(set) else set })
    }

    private fun withSession(transform: (ActiveSession) -> ActiveSession) {
        viewModelScope.launch {
            val current = repository.activeSession.first() ?: return@launch
            repository.saveActiveSession(transform(current))
        }
    }

    /**
     * Encerra o treino: grava no historico e monta o resumo.
     *
     * O grupo muscular de cada serie e copiado da biblioteca no momento da
     * gravacao em vez de ficar como referencia: se o exercicio for removido
     * depois, o historico precisa continuar contando para o volume daquele
     * grupo.
     */
    fun finishWorkout() {
        viewModelScope.launch {
            val session = repository.activeSession.first() ?: return@launch
            val now = System.currentTimeMillis()
            val snapshot = state.value

            val workout = snapshot.workouts.firstOrNull { it.id == session.workoutId }

            // Grava as series de todos os exercicios, nao so as do que estava
            // em foco: o usuario percorre o treino inteiro numa sessao.
            val completedSets = session.sets.filter { it.done }.mapNotNull { set ->
                val load = set.load.replace(',', '.').toDoubleOrNull() ?: return@mapNotNull null
                val reps = set.reps.toIntOrNull() ?: return@mapNotNull null
                val exercise = workout?.exercises?.getOrNull(set.exerciseIndex)
                val libraryEntry = snapshot.library.firstOrNull { it.id == set.exerciseId }
                CompletedSet(
                    exerciseId = set.exerciseId.ifEmpty { session.workoutId },
                    exerciseName = exercise?.name ?: session.exerciseName,
                    muscleGroup = libraryEntry?.group ?: MuscleGroup.Peito,
                    loadKg = load,
                    reps = reps,
                )
            }

            val completed = CompletedSession(
                id = "s-" + now,
                workoutId = session.workoutId,
                workoutLetter = session.workoutLetter,
                workoutName = session.workoutName,
                startedAtMillis = session.startedAtMillis,
                finishedAtMillis = now,
                effort = transient.value.effort,
                sets = completedSets,
            )

            val previousHistory = repository.history.first()
            val records = Stats.recordsOf(completed, previousHistory)
            val lastSame = previousHistory
                .filter { it.workoutId == completed.workoutId }
                .maxByOrNull { it.startedAtMillis }
            val delta = if (lastSame == null || lastSame.volumeKg <= 0.0) {
                "volume · primeiro registro"
            } else {
                val pct = ((completed.volumeKg - lastSame.volumeKg) / lastSame.volumeKg * 100).toInt()
                val sign = if (pct >= 0) "+" else ""
                "volume · " + sign + pct + "%"
            }

            repository.saveCompletedSession(completed)
            repository.clearActiveSession()

            transient.update {
                it.copy(
                    summary = WorkoutSummary(
                        sessionId = completed.id,
                        workoutName = completed.workoutName,
                        dateLabel = DateFormat.shortDate(completed.date),
                        timeRange = DateFormat.timeRange(
                            completed.startedAtMillis,
                            completed.finishedAtMillis,
                        ),
                        durationLabel = formatClock(completed.durationSeconds),
                        volumeKg = completed.volumeKg.toInt(),
                        volumeDelta = delta,
                        totalSets = completed.totalSets,
                        records = records,
                    ),
                )
            }
        }
    }

    /** O esforco e escolhido depois da sessao gravada, entao atualiza o registro. */
    // --------------------------------------------------------------- historico

    fun askDeleteSession(session: CompletedSession) =
        transient.update { it.copy(sessionToDelete = session) }

    fun dismissDeleteSession() = transient.update { it.copy(sessionToDelete = null) }

    /**
     * Apaga a sessao. As series vao junto pelo CASCADE da chave estrangeira,
     * e as estatisticas se refazem sozinhas porque derivam do historico.
     */
    fun confirmDeleteSession() {
        val id = transient.value.sessionToDelete?.id ?: return
        transient.update { it.copy(sessionToDelete = null) }
        viewModelScope.launch { repository.deleteSession(id) }
    }

    fun setEffort(index: Int) {
        val sessionId = transient.value.summary?.sessionId
        transient.update { it.copy(effort = index) }
        if (sessionId != null) {
            viewModelScope.launch { repository.updateSessionEffort(sessionId, index) }
        }
    }

    fun clearSummary() = transient.update { it.copy(summary = null, effort = 1) }

    // --------------------------------------------------------------- evolucao

    fun setPeriod(index: Int) = transient.update { it.copy(period = index) }

    /** Cicla entre os exercicios com progressao registrada. */
    fun nextProgressExercise() = transient.update {
        it.copy(progressExercise = it.progressExercise + 1)
    }

    // ------------------------------------------------------------------- peso

    fun openRecordWeight() = transient.update { it.copy(recordingWeight = true) }

    fun dismissRecordWeight() = transient.update { it.copy(recordingWeight = false) }

    fun recordWeight(text: String) {
        val kg = text.replace(',', '.').toDoubleOrNull()
        if (kg == null || kg <= 0) return
        transient.update { it.copy(recordingWeight = false) }
        viewModelScope.launch { repository.recordBodyWeight(kg) }
    }

    // ------------------------------------------------------------------ perfil

    fun openEditProfile() = transient.update { it.copy(editingProfile = true) }

    fun dismissEditProfile() = transient.update { it.copy(editingProfile = false) }

    fun saveProfile(name: String, since: String) {
        transient.update { it.copy(editingProfile = false) }
        viewModelScope.launch { repository.updateProfile(name, since) }
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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as SerieApplication
                SerieViewModel(app.repository)
            }
        }
    }
}

/** "32:14" a partir de segundos. */
fun formatClock(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%02d:%02d".format(m, s)
}

/** O que vem do banco. */
private data class PersistedState(
    val workouts: List<Workout>,
    val library: List<LibraryExercise>,
    val preferences: Preferences,
    val session: ActiveSession?,
    val profile: UserProfile,
    val history: List<CompletedSession>,
    val bodyWeights: List<BodyWeight>,
)

/** A parte do estado que vive so enquanto a tela esta aberta. */
private data class TransientState(
    val nowMillis: Long = System.currentTimeMillis(),
    val workoutFilter: Int = 0,
    val draft: Workout? = null,
    val draftIsNew: Boolean = false,
    val expandedExercise: Int = 1,
    val libraryQuery: String = "",
    val libraryGroup: Int = 0,
    val libraryPicked: Set<String> = emptySet(),
    /** Exercicio em edicao; nulo quando o dialogo esta fechado. */
    val exerciseDraft: LibraryExercise? = null,
    /** Verdadeiro quando o rascunho e um exercicio novo. */
    val exerciseDraftIsNew: Boolean = false,
    val exerciseError: String? = null,
    val sessionToDelete: CompletedSession? = null,
    val discardingWorkout: Boolean = false,
    val recordingWeight: Boolean = false,
    val editingProfile: Boolean = false,
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
    val exerciseDraft: LibraryExercise? = null,
    val exerciseDraftIsNew: Boolean = false,
    val exerciseError: String? = null,
    val sessionToDelete: CompletedSession? = null,
    val discardingWorkout: Boolean = false,
    val active: ActiveWorkout? = null,
    val summary: WorkoutSummary? = null,
    val effort: Int = 1,
    val period: Int = 1,
    val progressExercise: Int = 0,
    val preferences: Preferences = SeedData.preferences,
    val profile: UserProfile = SeedData.profile,
    val editingProfile: Boolean = false,
    // ---- derivados do historico ----
    val today: LocalDate = LocalDate.now(),
    val todayLabel: String = "",
    val history: List<CompletedSession> = emptyList(),
    val week: List<WeekDay> = emptyList(),
    val todaySessions: List<CompletedSession> = emptyList(),
    val bodyWeights: List<BodyWeight> = emptyList(),
    val recordingWeight: Boolean = false,
    val streak: Int = 0,
    val weekVolume: String = "0 kg",
    val sessionsThisWeek: Int = 0,
    val monthDelta: String = "-",
    val lastRecord: PersonalRecord? = null,
    val totalWorkouts: Int = 0,
    val totalRecords: Int = 0,
    val volumeChart: EvolutionPeriod? = null,
    val muscleVolumes: List<MuscleVolume> = emptyList(),
    val exerciseProgressList: List<ExerciseProgress> = emptyList(),
) {
    /** Abaixo do minimo, a evolucao mostra "poucos dados" em vez de graficos vazios. */
    val hasEnoughHistory: Boolean
        get() = history.size >= Stats.MIN_SESSIONS_FOR_CHARTS

    /** O exercicio em foco no card de progressao, se houver algum. */
    val progress: ExerciseProgress?
        get() = if (exerciseProgressList.isEmpty()) null
        else exerciseProgressList[progressExercise % exerciseProgressList.size]
}
