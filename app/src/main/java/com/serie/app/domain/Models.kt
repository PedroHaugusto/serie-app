package com.serie.app.domain

import androidx.compose.runtime.Immutable

/** Como o treino e classificado nos filtros da lista. */
enum class WorkoutType { Forca, Cardio }

/** Grupo muscular, usado para filtrar a biblioteca e somar volume. */
enum class MuscleGroup(val label: String) {
    Peito("Peito"),
    Costas("Costas"),
    Pernas("Pernas"),
    Ombros("Ombros"),
    Bracos("Braços"),
    Core("Core"),
}

/** Um exercicio do catalogo, independente de qualquer treino. */
@Immutable
data class LibraryExercise(
    val id: String,
    val name: String,
    val group: MuscleGroup,
    val equipment: String,
    /** Criado pelo usuario, em vez de vir do catalogo inicial. */
    val isCustom: Boolean = false,
)

/**
 * Um exercicio ja dentro de um treino, com a prescricao do usuario.
 *
 * [load] e texto e nao numero de proposito: o prototipo preve "corpo" para
 * peso corporal, e forcar um numero aqui obrigaria a inventar um campo extra
 * so para esse caso.
 */
@Immutable
data class WorkoutExercise(
    val id: String,
    val exerciseId: String,
    val name: String,
    val sets: Int,
    val reps: String,
    val load: String,
    val restSeconds: Int,
) {
    /** "4 × 8–10 · 70 kg · 90s" - a linha de resumo das listas. */
    val summary: String get() = "$sets × $reps · $load · ${restSeconds}s"
}

@Immutable
data class Workout(
    val id: String,
    /** A letra do treino: A, B, C... E a identidade visual dele no app. */
    val letter: String,
    val name: String,
    val type: WorkoutType,
    /** Dias da semana, de segunda a domingo. */
    val days: List<Boolean>,
    val exercises: List<WorkoutExercise>,
    val estimatedMinutes: Int,
    val muscleTags: List<String>,
) {
    val totalSets: Int get() = exercises.sumOf { it.sets }

    /** "6 exercícios · 55 min · Qua, Sex" */
    fun subtitle(): String {
        val dayLabels = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")
        val active = days.mapIndexedNotNull { i, on -> dayLabels[i].takeIf { on } }
        val when_ = if (active.isEmpty()) "livre" else active.joinToString(", ")
        val head = if (type == WorkoutType.Cardio) "Cardio" else "${exercises.size} exercícios"
        return "$head · $estimatedMinutes min · $when_"
    }
}

/** Uma serie registrada durante a execucao. */
@Immutable
data class SetEntry(
    val previous: String,
    val load: String,
    val reps: String,
    val done: Boolean,
)

/**
 * O treino em andamento, como ele e guardado.
 *
 * O tempo e ancorado em instantes do relogio, nao em contadores: se o app for
 * morto no meio do treino e reaberto dez minutos depois, o cronometro precisa
 * mostrar dez minutos a mais, nao continuar de onde parou. Contador decrescente
 * so funciona enquanto o processo vive - que e exatamente o que nao da para
 * assumir na academia.
 */
@Immutable
data class ActiveSession(
    val workoutId: String,
    val workoutLetter: String,
    val workoutName: String,
    val exerciseIndex: Int,
    val exerciseCount: Int,
    val exerciseName: String,
    val goal: String,
    val sets: List<SetEntry>,
    val startedAtMillis: Long,
    /** Instante em que o descanso termina; nulo quando nao esta correndo. */
    val restEndsAtMillis: Long?,
    /** Quanto sobra do descanso enquanto ele esta parado. */
    val restRemainingSeconds: Int,
) {
    fun elapsedSecondsAt(nowMillis: Long): Int =
        ((nowMillis - startedAtMillis) / 1000).coerceAtLeast(0).toInt()

    fun restSecondsAt(nowMillis: Long): Int = restEndsAtMillis
        ?.let { ((it - nowMillis + 999) / 1000).coerceAtLeast(0).toInt() }
        ?: restRemainingSeconds

    fun restRunningAt(nowMillis: Long): Boolean =
        restEndsAtMillis != null && restEndsAtMillis > nowMillis
}

/**
 * O treino em andamento como a tela le: com os tempos ja resolvidos para o
 * instante atual, para que a UI nao precise saber de relogio.
 */
@Immutable
data class ActiveWorkout(
    val workoutId: String,
    val workoutLetter: String,
    val workoutName: String,
    val exerciseIndex: Int,
    val exerciseCount: Int,
    val exerciseName: String,
    val goal: String,
    val sets: List<SetEntry>,
    val elapsedSeconds: Int,
    val restSeconds: Int,
    val restRunning: Boolean,
) {
    /** Indice da proxima serie nao concluida, ou -1 se todas fecharam. */
    val currentSetIndex: Int get() = sets.indexOfFirst { !it.done }
}

/** Resolve a sessao guardada para o instante [nowMillis]. */
fun ActiveSession.toView(nowMillis: Long) = ActiveWorkout(
    workoutId = workoutId,
    workoutLetter = workoutLetter,
    workoutName = workoutName,
    exerciseIndex = exerciseIndex,
    exerciseCount = exerciseCount,
    exerciseName = exerciseName,
    goal = goal,
    sets = sets,
    elapsedSeconds = elapsedSecondsAt(nowMillis),
    restSeconds = restSecondsAt(nowMillis),
    restRunning = restRunningAt(nowMillis),
)

/** Um recorde pessoal batido. */
@Immutable
data class PersonalRecord(
    val exerciseName: String,
    val detail: String,
)

/** O resultado de um treino concluido, exibido no resumo. */
@Immutable
data class WorkoutSummary(
    /** Id da sessao ja gravada no historico, para atualizar o esforco depois. */
    val sessionId: String,
    val workoutName: String,
    val dateLabel: String,
    val timeRange: String,
    val durationLabel: String,
    val volumeKg: Int,
    val volumeDelta: String,
    val totalSets: Int,
    val records: List<PersonalRecord>,
)

/** Como um dia aparece na faixa da semana da tela Hoje. */
enum class DayState { Done, Today, Planned, Rest }

@Immutable
data class WeekDay(
    val label: String,
    val state: DayState,
    /** Letra do treino planejado, quando houver. */
    val plannedLetter: String = "",
)

/** Uma linha da tabela de medidas corporais. */
@Immutable
data class BodyMeasure(
    val name: String,
    val before: String,
    val after: String,
    val delta: String,
    /** Se a variacao desejada e para baixo, como na cintura. */
    val lowerIsBetter: Boolean = false,
)

/** Um periodo do grafico de evolucao. */
@Immutable
data class EvolutionPeriod(
    val label: String,
    val volume: String,
    val delta: String,
    val axis: List<String>,
    val bars: List<Int>,
)

/** A progressao de carga de um exercicio ao longo do tempo. */
@Immutable
data class ExerciseProgress(
    val name: String,
    val estimated1RM: String,
    val from: String,
    val to: String,
    /** Pontos de 0 a 100 na vertical, do mais antigo ao mais recente. */
    val points: List<Float>,
)

@Immutable
data class MuscleVolume(
    val name: String,
    /** Participacao relativa, de 0 a 100, para o tamanho da barra. */
    val weight: Int,
    val percent: String,
)

@Immutable
data class UserProfile(
    val name: String,
    /** Quando comecou a treinar, como texto livre: "março de 2026". */
    val since: String,
    val workouts: Int,
    val streak: Int,
    val records: Int,
) {
    /**
     * A inicial do avatar deriva do nome em vez de ser um campo proprio:
     * guardada a parte, ficaria desatualizada no instante em que o usuario
     * trocasse o nome.
     */
    val initial: String get() = name.trim().firstOrNull()?.uppercase() ?: "?"
}

/** Preferencias do usuario, todas editaveis na tela de perfil. */
@Immutable
data class Preferences(
    val weeklyGoal: Int,
    val useKilograms: Boolean,
    val darkTheme: Boolean,
    val remindersOn: Boolean,
    val reminderLabel: String,
)

/** Estado de carregamento de uma tela de lista. */
enum class LoadState { Content, Empty, Loading, Error }

/** Uma serie registrada num treino concluido. */
@Immutable
data class CompletedSet(
    val exerciseId: String,
    val exerciseName: String,
    val muscleGroup: MuscleGroup,
    val loadKg: Double,
    val reps: Int,
) {
    /** Volume da serie: carga vezes repeticoes. */
    val volumeKg: Double get() = loadKg * reps

    /**
     * 1RM estimado pela formula de Epley.
     *
     * Serve para comparar series de reps diferentes na mesma escala - sem
     * isso, 70 kg x 10 e 90 kg x 3 nao teriam como ser ordenados.
     */
    val estimated1RM: Double get() = if (reps <= 1) loadKg else loadKg * (1 + reps / 30.0)
}

/** Um treino concluido, como ele fica no historico. */
@Immutable
data class CompletedSession(
    val id: String,
    val workoutId: String,
    val workoutLetter: String,
    val workoutName: String,
    val startedAtMillis: Long,
    val finishedAtMillis: Long,
    /** Esforco percebido: 0 leve, 1 ideal, 2 pesado. */
    val effort: Int,
    val sets: List<CompletedSet>,
) {
    val durationSeconds: Int
        get() = ((finishedAtMillis - startedAtMillis) / 1000).coerceAtLeast(0).toInt()

    val totalSets: Int get() = sets.size

    val volumeKg: Double get() = sets.sumOf { it.volumeKg }

    val date: java.time.LocalDate get() = DateFormat.dateOf(startedAtMillis)
}
