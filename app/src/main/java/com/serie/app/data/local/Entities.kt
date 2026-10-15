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
    /** 0 = veio do catalogo inicial, 1 = criado pelo usuario. */
    val isCustom: Boolean = false,
    /** Texto livre do usuario: dicas, cuidados, variacoes. */
    val notes: String = "",
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

/**
 * O treino em andamento, numa linha unica.
 *
 * Guardado a cada mudanca para que fechar o app no meio do treino - ou o
 * Android matar o processo em segundo plano, o que acontece justamente quando
 * o telefone fica no bolso durante a serie - nao perca o registro.
 */
@Entity(tableName = "active_session")
data class ActiveSessionEntity(
    @PrimaryKey val id: Int = SINGLE_ROW,
    val workoutId: String,
    val workoutLetter: String,
    val workoutName: String,
    val exerciseIndex: Int,
    val exerciseCount: Int,
    val exerciseId: String,
    val exerciseName: String,
    val goal: String,
    val startedAtMillis: Long,
    val restEndsAtMillis: Long?,
    val restRemainingSeconds: Int,
) {
    companion object {
        const val SINGLE_ROW = 1
    }
}

@Entity(tableName = "active_sets")
data class ActiveSetEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    /** A qual exercicio do treino esta serie pertence. */
    val exerciseIndex: Int = 0,
    val exerciseId: String = "",
    val position: Int,
    val previous: String,
    val load: String,
    val reps: String,
    val done: Boolean,
)

/**
 * O perfil do usuario, tambem numa linha unica.
 *
 * So nome e data de inicio: treinos, sequencia e recordes sao calculados do
 * historico e nao devem ser editaveis - um numero que o usuario digita nao e
 * uma estatistica.
 */
@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = SINGLE_ROW,
    val name: String,
    val since: String,
) {
    companion object {
        const val SINGLE_ROW = 1
    }
}

/** Um treino concluido. */
@Entity(tableName = "sessions", indices = [Index("startedAtMillis")])
data class SessionEntity(
    @PrimaryKey val id: String,
    val workoutId: String,
    val workoutLetter: String,
    val workoutName: String,
    val startedAtMillis: Long,
    val finishedAtMillis: Long,
    val effort: Int,
)

/**
 * As series daquele treino.
 *
 * Guardar serie a serie, e nao so o total, e o que permite derivar depois
 * volume por grupo muscular e progressao de carga por exercicio. Um campo
 * "volume total" economizaria espaco e fecharia a porta para as duas coisas.
 */
@Entity(
    tableName = "session_sets",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId"), Index("exerciseId")],
)
data class SessionSetEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val sessionId: String,
    val exerciseId: String,
    val exerciseName: String,
    val muscleGroup: String,
    val loadKg: Double,
    val reps: Int,
)

/** Uma sessao com suas series. */
data class SessionWithSets(
    @Embedded val session: SessionEntity,
    @Relation(parentColumn = "id", entityColumn = "sessionId")
    val sets: List<SessionSetEntity>,
)

/**
 * Historico de peso corporal.
 *
 * Uma linha por pesagem, e nao um campo unico no perfil: sem as anteriores
 * nao da para dizer se o peso evoluiu, que e a pergunta que a tela faz.
 */
@Entity(tableName = "body_weights", indices = [Index("atMillis")])
data class BodyWeightEntity(
    @PrimaryKey val id: String,
    val atMillis: Long,
    val kg: Double,
)
