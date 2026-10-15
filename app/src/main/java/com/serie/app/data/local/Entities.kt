package com.serie.app.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * Tabelas do banco.
 *
 * As entidades sao deliberadamente burras: so tipos que o SQLite entende
 * direto. Listas e enums viram texto aqui e voltam a ser tipos de dominio no
 * mapeamento do repositorio - assim o esquema do banco nao muda toda vez que
 * um modelo de dominio ganha um metodo.
 */

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey val id: String,
    val letter: String,
    val name: String,
    /** Nome da constante de WorkoutType. */
    val type: String,
    /** Sete caracteres "0"/"1", de segunda a domingo. */
    val days: String,
    val estimatedMinutes: Int,
    /** Rotulos separados por "|". */
    val muscleTags: String,
    /** Ordem na lista de treinos; o usuario pode reordenar depois. */
    val position: Int,
)

@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            // Apagar o treino apaga os exercicios dele. A alternativa seria
            // limpar na mao em toda exclusao, e uma falha nisso deixaria
            // exercicios orfaos ocupando espaco para sempre.
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("workoutId")],
)
data class WorkoutExerciseEntity(
    @PrimaryKey val id: String,
    val workoutId: String,
    val exerciseId: String,
    val name: String,
    val sets: Int,
    val reps: String,
    val load: String,
    val restSeconds: Int,
    /** Ordem dentro do treino - e o que a reordenacao do editor grava. */
    val position: Int,
)

@Entity(tableName = "library_exercises")
data class LibraryExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    /** Nome da constante de MuscleGroup. */
    val muscleGroup: String,
    val equipment: String,
)

/**
 * Preferencias do usuario numa linha unica.
 *
 * [id] e fixo em 1: a tabela existe para guardar um registro so, e a chave
 * constante faz todo upsert sobrescrever em vez de acumular linhas.
 */
@Entity(tableName = "preferences")
data class PreferencesEntity(
    @PrimaryKey val id: Int = SINGLE_ROW,
    val weeklyGoal: Int,
    val useKilograms: Boolean,
    val darkTheme: Boolean,
    val remindersOn: Boolean,
    val reminderLabel: String,
) {
    companion object {
        const val SINGLE_ROW = 1
    }
}

/** Um treino com seus exercicios, montado pelo Room numa consulta so. */
data class WorkoutWithExercises(
    @Embedded val workout: WorkoutEntity,
    @Relation(parentColumn = "id", entityColumn = "workoutId")
    val exercises: List<WorkoutExerciseEntity>,
)
