package com.serie.app.data

import com.serie.app.domain.BodyMeasure
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

    /** O treino de hoje no prototipo e o B. */
    const val TODAY_WORKOUT_ID = "w-b"

    val week: List<WeekDay> = listOf(
        WeekDay("S", DayState.Done),
        WeekDay("T", DayState.Rest),
        WeekDay("Q", DayState.Done),
        WeekDay("Q", DayState.Done),
        WeekDay("S", DayState.Today, "B"),
        WeekDay("S", DayState.Planned, "D"),
        WeekDay("D", DayState.Rest),
    )

    const val TODAY_LABEL = "Quarta, 14 de outubro"
    const val WEEK_PROGRESS = "3 de 5 treinos"

    const val STREAK_DAYS = "12"
    const val WEEK_VOLUME = "18,4 t"
    const val MONTH_DELTA = "+14%"

    val lastRecord = PersonalRecord("Supino reto · 82,5 kg", "Novo recorde na segunda · +2,5 kg")

    val readyPrograms = listOf(
        "Push / Pull / Legs" to "6× semana",
        "ABC clássico" to "3–6× semana",
        "Full body" to "3× semana",
    )

    val periods: List<EvolutionPeriod> = listOf(
        EvolutionPeriod(
            "4 sem", "18,4 t", "+6% vs. anterior", listOf("seg", "qua", "sex", "dom"),
            listOf(40, 0, 62, 55, 0, 70, 0, 58, 0, 66, 72, 90),
        ),
        EvolutionPeriod(
            "3 meses", "72,6 t", "+14% vs. anterior", listOf("jul", "ago", "set", "out"),
            listOf(42, 50, 38, 56, 60, 48, 66, 70, 64, 78, 84, 96),
        ),
        EvolutionPeriod(
            "6 meses", "141 t", "+22% vs. anterior", listOf("mai", "jun", "jul", "out"),
            listOf(30, 36, 34, 44, 40, 52, 50, 58, 62, 70, 78, 92),
        ),
        EvolutionPeriod(
            "1 ano", "286 t", "+31% vs. anterior", listOf("nov", "fev", "mai", "out"),
            listOf(20, 26, 30, 28, 38, 44, 46, 52, 60, 66, 80, 94),
        ),
    )

    /**
     * Os pontos vem do prototipo como coordenadas SVG, onde y cresce para
     * baixo num quadro de 80. Aqui viram altura relativa de 0 a 100 com y
     * crescendo para cima, que e como o grafico desenha.
     */
    private fun svgToHeights(vararg y: Int) = y.map { (80 - it) / 80f * 100f }

    val exerciseProgress: List<ExerciseProgress> = listOf(
        ExerciseProgress(
            "Supino reto", "98 kg", "70 kg em julho", "82,5 kg hoje · recorde",
            svgToHeights(66, 62, 62, 54, 50, 50, 40, 32, 26, 12),
        ),
        ExerciseProgress(
            "Agachamento livre", "131 kg", "90 kg em julho", "110 kg hoje",
            svgToHeights(70, 66, 58, 58, 48, 44, 40, 34, 30, 22),
        ),
        ExerciseProgress(
            "Remada curvada", "89 kg", "60 kg em julho", "72,5 kg hoje · recorde",
            svgToHeights(62, 60, 56, 56, 50, 46, 46, 36, 30, 18),
        ),
    )

    val muscleVolumes: List<MuscleVolume> = listOf(
        MuscleVolume("Costas", 88, "24%"),
        MuscleVolume("Peito", 76, "21%"),
        MuscleVolume("Pernas", 62, "17%"),
        MuscleVolume("Braços", 50, "14%"),
        MuscleVolume("Ombros", 28, "8%"),
    )

    const val TOTAL_WORKOUTS = "38"
    const val WORKOUTS_PER_WEEK = "treinos · 3,2 por semana"
    const val TOTAL_RECORDS = "9"
    const val MUSCLE_ALERT = "Ombros abaixo da meta"

    const val BODY_WEIGHT = "78,4"
    const val BODY_WEIGHT_DELTA = "−2,1 kg"
    const val BODY_WEIGHT_PERIOD = "em 90 dias"
    const val HEALTH_SYNC = "Sincronizado com Health Connect · hoje 07:12"

    val measures: List<BodyMeasure> = listOf(
        BodyMeasure("Braço", "36,0", "37,5", "+1,5"),
        BodyMeasure("Peito", "101", "103", "+2,0"),
        BodyMeasure("Cintura", "86,0", "82,5", "−3,5", lowerIsBetter = true),
        BodyMeasure("Coxa", "57,0", "58,5", "+1,5"),
    )

    val progressPhotos = listOf("14 jul · frente", "14 out · frente")

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

    /** As series ja registradas do exercicio em andamento no prototipo. */
    val activeSets = listOf(
        Triple("67,5 × 10", "70", "10"),
        Triple("67,5 × 9", "70", "9"),
        Triple("67,5 × 8", "70", "8"),
        Triple("65 × 8", "70", "8"),
    )

    val finishedRecords = listOf(
        PersonalRecord("Remada curvada", "72,5 kg × 8 · +2,5 kg"),
        PersonalRecord("Barra fixa", "9 repetições · +1 rep"),
    )
}
