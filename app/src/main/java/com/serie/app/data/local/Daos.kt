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

    @Query("SELECT * FROM library_exercises ORDER BY rowid")
    fun observeAll(): Flow<List<LibraryExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<LibraryExerciseEntity>)

    @Query("SELECT COUNT(*) FROM library_exercises")
    suspend fun count(): Int
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
