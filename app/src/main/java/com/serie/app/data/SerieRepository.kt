package com.serie.app.data

import com.serie.app.data.local.ActiveSessionDao
import com.serie.app.data.local.ActiveSessionEntity
import com.serie.app.data.local.ActiveSetEntity
import com.serie.app.data.local.LibraryDao
import com.serie.app.data.local.LibraryExerciseEntity
import com.serie.app.data.local.PreferencesDao
import com.serie.app.data.local.ProfileDao
import com.serie.app.data.local.SessionDao
import com.serie.app.data.local.SessionEntity
import com.serie.app.data.local.SessionSetEntity
import com.serie.app.data.local.SessionWithSets
import com.serie.app.data.local.ProfileEntity
import com.serie.app.data.local.PreferencesEntity
import com.serie.app.data.local.SerieDatabase
import com.serie.app.data.local.WorkoutDao
import com.serie.app.data.local.WorkoutEntity
import com.serie.app.data.local.WorkoutExerciseEntity
import com.serie.app.data.local.WorkoutWithExercises
import com.serie.app.domain.ActiveSession
import com.serie.app.domain.CompletedSession
import com.serie.app.domain.CompletedSet
import com.serie.app.domain.LibraryExercise
import com.serie.app.domain.MuscleGroup
import com.serie.app.domain.Preferences
import com.serie.app.domain.UserProfile
import com.serie.app.domain.SetEntry
import com.serie.app.domain.Workout
import com.serie.app.domain.WorkoutExercise
import com.serie.app.domain.WorkoutType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Porta de entrada dos dados persistidos.
 *
 * Traduz entre as entidades do banco e os modelos de dominio, para que as
 * telas e o ViewModel nunca precisem saber que listas viram texto no SQLite.
 */
class SerieRepository(
    private val workoutDao: WorkoutDao,
    private val libraryDao: LibraryDao,
    private val preferencesDao: PreferencesDao,
    private val activeSessionDao: ActiveSessionDao,
    private val profileDao: ProfileDao,
    private val sessionDao: SessionDao,
) {

    constructor(database: SerieDatabase) : this(
        database.workoutDao(),
        database.libraryDao(),
        database.preferencesDao(),
        database.activeSessionDao(),
        database.profileDao(),
        database.sessionDao(),
    )

    val workouts: Flow<List<Workout>> =
        workoutDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    val library: Flow<List<LibraryExercise>> =
        libraryDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    /**
     * O perfil. Cai no padrao enquanto a linha nao existe - caso de quem
     * atualizou o app vindo de uma versao anterior ao perfil editavel.
     */
    val profile: Flow<UserProfile> =
        profileDao.observe().map { row ->
            row?.let { SeedData.profile.copy(name = it.name, since = it.since) }
                ?: SeedData.profile
        }

    suspend fun updateProfile(name: String, since: String) {
        val cleanName = name.trim().ifEmpty { SeedData.profile.name }
        profileDao.upsert(ProfileEntity(name = cleanName, since = since.trim()))
    }

    val preferences: Flow<Preferences?> =
        preferencesDao.observe().map { it?.toDomain() }

    /**
     * O treino em andamento, se houver.
     *
     * Sessao e series vivem em tabelas separadas e sao combinadas aqui, para
     * que o resto do app veja um objeto so.
     */
    val activeSession: Flow<ActiveSession?> =
        combine(
            activeSessionDao.observeSession(),
            activeSessionDao.observeSets(),
        ) { session, sets ->
            session?.toDomain(sets.sortedBy { it.position }.map { it.toDomain() })
        }

    /** O historico de treinos concluidos, do mais recente para o mais antigo. */
    val history: Flow<List<CompletedSession>> =
        sessionDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    suspend fun saveCompletedSession(session: CompletedSession) =
        sessionDao.save(
            session = session.toEntity(),
            sets = session.sets.map { it.toEntity(session.id) },
        )

    suspend fun updateSessionEffort(id: String, effort: Int) =
        sessionDao.updateEffort(id, effort)

    suspend fun saveActiveSession(session: ActiveSession) =
        activeSessionDao.save(
            session = session.toEntity(),
            sets = session.sets.mapIndexed { i, set -> set.toEntity(i) },
        )

    suspend fun clearActiveSession() = activeSessionDao.clear()

    /**
     * Cria um exercicio no catalogo.
     *
     * Devolve nulo quando ja existe outro com o mesmo nome: duplicar
     * "Supino reto" deixaria a busca ambigua e o historico de carga dividido
     * entre dois registros que o usuario enxerga como um so.
     */
    suspend fun createExercise(
        name: String,
        group: MuscleGroup,
        equipment: String,
        notes: String,
    ): LibraryExercise? {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || libraryDao.countByName(trimmed) > 0) return null
        val exercise = LibraryExercise(
            id = "custom-${System.currentTimeMillis()}",
            name = trimmed,
            group = group,
            equipment = equipment.trim().ifEmpty { "Livre" },
            isCustom = true,
            notes = notes.trim(),
        )
        libraryDao.upsert(exercise.toEntity())
        return exercise
    }

    /**
     * Edita um exercicio existente.
     *
     * Vale tambem para os do catalogo inicial: a biblioteca e do usuario, e
     * uma dica de execucao so serve se puder ser escrita no exercicio que ele
     * de fato usa.
     */
    suspend fun updateExercise(exercise: LibraryExercise): Boolean {
        val trimmed = exercise.name.trim()
        if (trimmed.isEmpty()) return false
        val clash = libraryDao.findByName(trimmed)
        if (clash != null && clash.id != exercise.id) return false
        libraryDao.upsert(
            exercise.copy(
                name = trimmed,
                equipment = exercise.equipment.trim().ifEmpty { "Livre" },
                notes = exercise.notes.trim(),
            ).toEntity()
        )
        return true
    }

    suspend fun deleteSession(id: String) = sessionDao.delete(id)

    suspend fun deleteCustomExercise(id: String) = libraryDao.deleteCustom(id)

    suspend fun saveWorkout(workout: Workout, position: Int) {
        val existing = workoutDao.findById(workout.id)
        workoutDao.save(
            workout = workout.toEntity(existing?.workout?.position ?: position),
            exercises = workout.exercises.mapIndexed { i, e -> e.toEntity(workout.id, i) },
        )
    }

    suspend fun deleteWorkout(id: String) = workoutDao.deleteWorkout(id)

    suspend fun workoutCount(): Int = workoutDao.count()

    suspend fun updatePreferences(preferences: Preferences) =
        preferencesDao.upsert(preferences.toEntity())

    /**
     * Carrega os dados iniciais na primeira execucao.
     *
     * O marcador de "ja semeado" e a existencia da linha de preferencias, nao
     * a lista de treinos estar vazia: quem apagar todos os treinos de
     * proposito nao quer ve-los voltar no proximo lancamento.
     */
    suspend fun seedIfFirstRun() {
        if (preferencesDao.find() != null) return

        libraryDao.insertAll(SeedData.library.map { it.toEntity() })
        SeedData.workouts.forEachIndexed { index, workout ->
            workoutDao.save(
                workout = workout.toEntity(index),
                exercises = workout.exercises.mapIndexed { i, e -> e.toEntity(workout.id, i) },
            )
        }
        preferencesDao.upsert(SeedData.preferences.toEntity())
        profileDao.upsert(
            ProfileEntity(name = SeedData.profile.name, since = SeedData.profile.since)
        )
    }
}

// ----------------------------------------------------------------- mapeamento

private const val TAG_SEPARATOR = "|"

private fun WorkoutWithExercises.toDomain() = Workout(
    id = workout.id,
    letter = workout.letter,
    name = workout.name,
    type = runCatching { WorkoutType.valueOf(workout.type) }.getOrDefault(WorkoutType.Forca),
    days = workout.days.map { it == '1' },
    exercises = exercises.sortedBy { it.position }.map { it.toDomain() },
    estimatedMinutes = workout.estimatedMinutes,
    muscleTags = workout.muscleTags.split(TAG_SEPARATOR).filter { it.isNotBlank() },
)

private fun Workout.toEntity(position: Int) = WorkoutEntity(
    id = id,
    letter = letter,
    name = name,
    type = type.name,
    days = days.joinToString("") { if (it) "1" else "0" },
    estimatedMinutes = estimatedMinutes,
    muscleTags = muscleTags.joinToString(TAG_SEPARATOR),
    position = position,
)

private fun WorkoutExerciseEntity.toDomain() = WorkoutExercise(
    id = id,
    exerciseId = exerciseId,
    name = name,
    sets = sets,
    reps = reps,
    load = load,
    restSeconds = restSeconds,
)

private fun WorkoutExercise.toEntity(workoutId: String, position: Int) = WorkoutExerciseEntity(
    id = id,
    workoutId = workoutId,
    exerciseId = exerciseId,
    name = name,
    sets = sets,
    reps = reps,
    load = load,
    restSeconds = restSeconds,
    position = position,
)

private fun LibraryExerciseEntity.toDomain() = LibraryExercise(
    id = id,
    name = name,
    group = runCatching { MuscleGroup.valueOf(muscleGroup) }.getOrDefault(MuscleGroup.Peito),
    equipment = equipment,
    isCustom = isCustom,
    notes = notes,
)

private fun LibraryExercise.toEntity() = LibraryExerciseEntity(
    id = id,
    name = name,
    muscleGroup = group.name,
    equipment = equipment,
    isCustom = isCustom,
    notes = notes,
)

private fun ActiveSessionEntity.toDomain(sets: List<SetEntry>) = ActiveSession(
    workoutId = workoutId,
    workoutLetter = workoutLetter,
    workoutName = workoutName,
    exerciseIndex = exerciseIndex,
    exerciseCount = exerciseCount,
    exerciseId = exerciseId,
    exerciseName = exerciseName,
    goal = goal,
    sets = sets,
    startedAtMillis = startedAtMillis,
    restEndsAtMillis = restEndsAtMillis,
    restRemainingSeconds = restRemainingSeconds,
)

private fun ActiveSession.toEntity() = ActiveSessionEntity(
    workoutId = workoutId,
    workoutLetter = workoutLetter,
    workoutName = workoutName,
    exerciseIndex = exerciseIndex,
    exerciseCount = exerciseCount,
    exerciseId = exerciseId,
    exerciseName = exerciseName,
    goal = goal,
    startedAtMillis = startedAtMillis,
    restEndsAtMillis = restEndsAtMillis,
    restRemainingSeconds = restRemainingSeconds,
)

private fun SessionWithSets.toDomain() = CompletedSession(
    id = session.id,
    workoutId = session.workoutId,
    workoutLetter = session.workoutLetter,
    workoutName = session.workoutName,
    startedAtMillis = session.startedAtMillis,
    finishedAtMillis = session.finishedAtMillis,
    effort = session.effort,
    sets = sets.map { it.toDomain() },
)

private fun CompletedSession.toEntity() = SessionEntity(
    id = id,
    workoutId = workoutId,
    workoutLetter = workoutLetter,
    workoutName = workoutName,
    startedAtMillis = startedAtMillis,
    finishedAtMillis = finishedAtMillis,
    effort = effort,
)

private fun SessionSetEntity.toDomain() = CompletedSet(
    exerciseId = exerciseId,
    exerciseName = exerciseName,
    muscleGroup = runCatching { MuscleGroup.valueOf(muscleGroup) }
        .getOrDefault(MuscleGroup.Peito),
    loadKg = loadKg,
    reps = reps,
)

private fun CompletedSet.toEntity(sessionId: String) = SessionSetEntity(
    sessionId = sessionId,
    exerciseId = exerciseId,
    exerciseName = exerciseName,
    muscleGroup = muscleGroup.name,
    loadKg = loadKg,
    reps = reps,
)

private fun ActiveSetEntity.toDomain() = SetEntry(
    previous = previous,
    load = load,
    reps = reps,
    done = done,
)

private fun SetEntry.toEntity(position: Int) = ActiveSetEntity(
    position = position,
    previous = previous,
    load = load,
    reps = reps,
    done = done,
)

private fun PreferencesEntity.toDomain() = Preferences(
    weeklyGoal = weeklyGoal,
    useKilograms = useKilograms,
    darkTheme = darkTheme,
    remindersOn = remindersOn,
    reminderLabel = reminderLabel,
)

private fun Preferences.toEntity() = PreferencesEntity(
    weeklyGoal = weeklyGoal,
    useKilograms = useKilograms,
    darkTheme = darkTheme,
    remindersOn = remindersOn,
    reminderLabel = reminderLabel,
)
