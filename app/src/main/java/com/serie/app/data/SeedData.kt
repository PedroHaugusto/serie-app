package com.serie.app.data

import com.serie.app.domain.MeasureType
import com.serie.app.domain.DayState
import com.serie.app.domain.EvolutionPeriod
import com.serie.app.domain.ExerciseProgress
import com.serie.app.domain.LibraryExercise
import com.serie.app.domain.MuscleGroup
import com.serie.app.domain.MuscleVolume
import com.serie.app.domain.PersonalRecord
import com.serie.app.domain.Preferences
import com.serie.app.domain.UserProfile
import com.serie.app.domain.WeekDay
import com.serie.app.domain.Workout
import com.serie.app.domain.WorkoutExercise
import com.serie.app.domain.WorkoutType

/**
 * Dados iniciais, copiados do prototipo.
 *
 * Existem para que o app abra com conteudo em vez de telas vazias enquanto a
 * persistencia nao entra. Quando o Room assumir, isto vira a carga inicial do
 * banco - nao some, muda de lugar.
 */
object SeedData {

    val library: List<LibraryExercise> = listOf(
        Triple("Supino reto", MuscleGroup.Peito, "Barra"),
        Triple("Supino inclinado", MuscleGroup.Peito, "Halteres"),
        Triple("Crucifixo", MuscleGroup.Peito, "Máquina"),
        Triple("Barra fixa", MuscleGroup.Costas, "Peso corporal"),
        Triple("Remada curvada", MuscleGroup.Costas, "Barra"),
        Triple("Puxada alta", MuscleGroup.Costas, "Polia"),
        Triple("Agachamento livre", MuscleGroup.Pernas, "Barra"),
        Triple("Leg press 45°", MuscleGroup.Pernas, "Máquina"),
        Triple("Stiff", MuscleGroup.Pernas, "Barra"),
        Triple("Desenvolvimento", MuscleGroup.Ombros, "Halteres"),
        Triple("Elevação lateral", MuscleGroup.Ombros, "Halteres"),
        Triple("Rosca direta", MuscleGroup.Bracos, "Barra"),
        Triple("Tríceps corda", MuscleGroup.Bracos, "Polia"),
        Triple("Prancha", MuscleGroup.Core, "Peso corporal"),
    ).mapIndexed { i, (name, group, equipment) ->
        LibraryExercise("lib-$i", name, group, equipment)
    }

    private fun exercisesOfB(): List<WorkoutExercise> = listOf(
        WorkoutExercise("b-0", "lib-3", "Barra fixa", 4, "6–8", "corpo", 120),
        WorkoutExercise("b-1", "lib-4", "Remada curvada", 4, "8–10", "70 kg", 90),
        WorkoutExercise("b-2", "lib-5", "Puxada alta", 3, "10–12", "55 kg", 90),
        WorkoutExercise("b-3", "lib-11", "Rosca direta", 3, "10", "30 kg", 60),
        WorkoutExercise("b-4", "lib-11", "Rosca martelo", 3, "12", "14 kg", 60),
        WorkoutExercise("b-5", "lib-5", "Face pull", 3, "15", "20 kg", 45),
    )

    /** Dias: segunda a domingo. */
    private fun days(vararg active: Int) = List(7) { it in active.toList() }

    val workouts: List<Workout> = listOf(
        Workout(
            id = "w-a", letter = "A", name = "Peito e tríceps", type = WorkoutType.Forca,
            days = days(0, 3), exercises = List(7) { i ->
                WorkoutExercise("a-$i", "lib-0", "Exercício ${i + 1}", 3, "8–12", "40 kg", 90)
            },
            estimatedMinutes = 60, muscleTags = listOf("Peito", "Tríceps", "Ombros"),
        ),
        Workout(
            id = "w-b", letter = "B", name = "Costas e bíceps", type = WorkoutType.Forca,
            days = days(2, 4), exercises = exercisesOfB(),
            estimatedMinutes = 55, muscleTags = listOf("Costas", "Bíceps", "Antebraço"),
        ),
        Workout(
            id = "w-c", letter = "C", name = "Pernas completo", type = WorkoutType.Forca,
            days = days(1), exercises = List(8) { i ->
                WorkoutExercise("c-$i", "lib-6", "Exercício ${i + 1}", 4, "8–10", "80 kg", 120)
            },
            estimatedMinutes = 70, muscleTags = listOf("Quadríceps", "Posterior", "Glúteo"),
        ),
        Workout(
            id = "w-d", letter = "D", name = "Ombros e core", type = WorkoutType.Forca,
            days = days(5), exercises = List(5) { i ->
                WorkoutExercise("d-$i", "lib-9", "Exercício ${i + 1}", 3, "12", "16 kg", 60)
            },
            estimatedMinutes = 45, muscleTags = listOf("Ombros", "Core"),
        ),
        Workout(
            id = "w-e", letter = "E", name = "Corrida intervalada", type = WorkoutType.Cardio,
            days = days(), exercises = emptyList(),
            estimatedMinutes = 30, muscleTags = listOf("Cardio"),
        ),
    )


    val readyPrograms = listOf(
        "Push / Pull / Legs" to "6× semana",
        "ABC clássico" to "3–6× semana",
        "Full body" to "3× semana",
    )

    const val BODY_WEIGHT = "78,4"
    const val BODY_WEIGHT_DELTA = "−2,1 kg"
    const val BODY_WEIGHT_PERIOD = "em 90 dias"
    const val HEALTH_SYNC = "Sincronizado com Health Connect · hoje 07:12"

    /**
     * As medidas que o app ja traz. O usuario pode criar outras, e estas
     * comecam sem nenhuma medicao - a tabela mostra tracos ate ele medir.
     */
    val measureTypes: List<MeasureType> = listOf(
        MeasureType("mt-braco", "Braço"),
        MeasureType("mt-peito", "Peito"),
        MeasureType("mt-cintura", "Cintura", lowerIsBetter = true),
        MeasureType("mt-coxa", "Coxa"),
    )

    val profile = UserProfile(
        name = "Rafael Lima",
        since = "março de 2026",
        workouts = 38,
        streak = 12,
        records = 9,
    )

    val preferences = Preferences(
        weeklyGoal = 5,
        useKilograms = true,
        darkTheme = true,
        remindersOn = true,
        reminderLabel = "Seg, Qua, Sex · 18:00",
    )

}
