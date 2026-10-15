package com.serie.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    /**
     * Observa os treinos com seus exercicios.
     *
     * `@Transaction` nao e enfeite: o Room resolve a relacao em duas consultas
     * (treinos, depois exercicios), e sem a transacao uma escrita no meio das
     * duas devolveria um treino com os exercicios de outro estado.
     */
    @Transaction
    @Query("SELECT * FROM workouts ORDER BY position")
    fun observeAll(): Flow<List<WorkoutWithExercises>>

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun findById(id: String): WorkoutWithExercises?

    @Upsert
    suspend fun upsertWorkout(workout: WorkoutEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<WorkoutExerciseEntity>)

    @Query("DELETE FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun deleteExercisesOf(workoutId: String)

    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun deleteWorkout(id: String)

    @Query("SELECT COUNT(*) FROM workouts")
    suspend fun count(): Int

    /**
     * Grava o treino inteiro de uma vez.
     *
     * Os exercicios sao apagados e reinseridos em vez de reconciliados: o
     * editor pode remover, adicionar e reordenar na mesma sessao, e comparar
     * duas listas para descobrir o que mudou daria o mesmo resultado com mais
     * chance de erro. Sao poucas linhas por treino.
     */
    @Transaction
    suspend fun save(workout: WorkoutEntity, exercises: List<WorkoutExerciseEntity>) {
        upsertWorkout(workout)
        deleteExercisesOf(workout.id)
        insertExercises(exercises)
    }
}

@Dao
interface LibraryDao {

    /** Os criados pelo usuario vem primeiro: sao os que ele acabou de procurar. */
    @Query("SELECT * FROM library_exercises ORDER BY isCustom DESC, name")
    fun observeAll(): Flow<List<LibraryExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<LibraryExerciseEntity>)

    @Upsert
    suspend fun upsert(exercise: LibraryExerciseEntity)

    @Query("DELETE FROM library_exercises WHERE id = :id AND isCustom = 1")
    suspend fun deleteCustom(id: String)

    /** Usado para avisar que o nome ja existe antes de criar um duplicado. */
    @Query("SELECT COUNT(*) FROM library_exercises WHERE LOWER(name) = LOWER(:name)")
    suspend fun countByName(name: String): Int

    @Query("SELECT COUNT(*) FROM library_exercises")
    suspend fun count(): Int
}

/**
 * Acesso ao treino em andamento.
 *
 * Sessao e series andam juntas - gravar uma sem a outra deixaria o app
 * reabrindo num estado impossivel - entao as escritas passam por [save], que
 * e transacional.
 */
@Dao
interface ActiveSessionDao {

    @Query("SELECT * FROM active_session WHERE id = :id")
    fun observeSession(id: Int = ActiveSessionEntity.SINGLE_ROW): Flow<ActiveSessionEntity?>

    @Query("SELECT * FROM active_sets ORDER BY position")
    fun observeSets(): Flow<List<ActiveSetEntity>>

    @Upsert
    suspend fun upsertSession(session: ActiveSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<ActiveSetEntity>)

    @Query("DELETE FROM active_sets")
    suspend fun clearSets()

    @Query("DELETE FROM active_session")
    suspend fun clearSession()

    @Transaction
    suspend fun save(session: ActiveSessionEntity, sets: List<ActiveSetEntity>) {
        upsertSession(session)
        clearSets()
        insertSets(sets)
    }

    @Transaction
    suspend fun clear() {
        clearSets()
        clearSession()
    }
}

@Dao
interface PreferencesDao {

    @Query("SELECT * FROM preferences WHERE id = :id")
    fun observe(id: Int = PreferencesEntity.SINGLE_ROW): Flow<PreferencesEntity?>

    @Query("SELECT * FROM preferences WHERE id = :id")
    suspend fun find(id: Int = PreferencesEntity.SINGLE_ROW): PreferencesEntity?

    @Upsert
    suspend fun upsert(preferences: PreferencesEntity)
}

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profile WHERE id = :id")
    fun observe(id: Int = ProfileEntity.SINGLE_ROW): Flow<ProfileEntity?>

    @Query("SELECT * FROM profile WHERE id = :id")
    suspend fun find(id: Int = ProfileEntity.SINGLE_ROW): ProfileEntity?

    @Upsert
    suspend fun upsert(profile: ProfileEntity)
}
