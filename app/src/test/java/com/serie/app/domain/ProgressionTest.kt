package com.serie.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A progressao dupla decide sozinha quando subir a carga, entao as regras
 * precisam estar presas por teste: errar para mais faz o usuario subir peso
 * que nao sustenta, e errar para menos o deixa parado numa carga ja facil.
 */
class ProgressionTest {

    @Test
    fun `le faixas escritas de varias formas`() {
        assertEquals(RepRange(10, 12), Stats.parseRepRange("10-12"))
        assertEquals(RepRange(8, 10), Stats.parseRepRange("8–10"))
        assertEquals(RepRange(10, 12), Stats.parseRepRange(" 10 - 12 "))
        assertEquals(RepRange(10, 10), Stats.parseRepRange("10"))
        assertNull(Stats.parseRepRange("ate falhar"))
    }

    @Test
    fun `fechar o topo da faixa em todas as series sobe a carga`() {
        val advices = advicesFor(reps = "10-12", done = listOf(12, 12, 12), load = 40.0)
        assertEquals(1, advices.size)
        val advice = advices.first()
        assertEquals(40.0, advice.currentLoadKg, 0.001)
        assertEquals(42.5, advice.suggestedLoadKg, 0.001)
        assertEquals(RepRange(10, 12), advice.range)
        assertTrue(advice.action.contains("42,5 kg"))
        assertTrue(advice.action.contains("volte a 10 reps"))
    }

    @Test
    fun `passar do topo tambem sobe a carga`() {
        val advices = advicesFor(reps = "10-12", done = listOf(14, 13, 12), load = 40.0)
        assertEquals(1, advices.size)
        // A recomendacao reporta a serie mais fraca: e ela que prova que a
        // faixa foi fechada em todas, nao a melhor do dia.
        assertEquals(12, advices.first().achievedReps)
    }

    @Test
    fun `uma serie abaixo do topo segura a carga`() {
        assertTrue(advicesFor(reps = "10-12", done = listOf(12, 12, 11), load = 40.0).isEmpty())
    }

    @Test
    fun `exercicio sem carga fica de fora`() {
        // Peso corporal: a progressao ali e em repeticoes, e "+2,5 kg" nao
        // seria uma instrucao executavel.
        assertTrue(advicesFor(reps = "10-12", done = listOf(12, 12, 12), load = 0.0).isEmpty())
    }

    @Test
    fun `prescricao de um numero so pede para manter as repeticoes`() {
        val advice = advicesFor(reps = "10", done = listOf(10, 10, 10), load = 5.0).single()
        assertEquals(7.5, advice.suggestedLoadKg, 0.001)
        assertTrue(advice.action.contains("mantenha as 10 reps"))
        assertTrue(advice.reason.contains("a meta de 10"))
    }

    @Test
    fun `so a sessao mais recente conta`() {
        val workouts = listOf(workoutWith(reps = "10-12"))
        val history = listOf(
            sessionAt(millis = 1_000, done = listOf(12, 12, 12), load = 40.0),
            sessionAt(millis = 2_000, done = listOf(10, 10, 10), load = 42.5),
        )
        // A ultima sessao ficou no piso da faixa: subir de novo seria empilhar
        // carga sobre um treino que ainda nao foi fechado.
        assertTrue(Stats.progressionAdvices(history, workouts).isEmpty())
    }

    @Test
    fun `sem historico do exercicio nao ha recomendacao`() {
        assertTrue(Stats.progressionAdvices(emptyList(), listOf(workoutWith("10-12"))).isEmpty())
    }

    // ------------------------------------------------------------- fixtures

    private fun advicesFor(reps: String, done: List<Int>, load: Double) =
        Stats.progressionAdvices(
            listOf(sessionAt(millis = 1_000, done = done, load = load)),
            listOf(workoutWith(reps)),
        )

    private fun workoutWith(reps: String) = Workout(
        id = "w1",
        letter = "A",
        name = "Teste",
        type = WorkoutType.Forca,
        days = List(7) { false },
        exercises = listOf(
            WorkoutExercise(
                id = "we1",
                exerciseId = "ex1",
                name = "Agachamento",
                sets = 3,
                reps = reps,
                load = "—",
                restSeconds = 90,
            ),
        ),
        estimatedMinutes = 45,
        muscleTags = emptyList(),
    )

    private fun sessionAt(millis: Long, done: List<Int>, load: Double) = CompletedSession(
        id = "s$millis",
        workoutId = "w1",
        workoutLetter = "A",
        workoutName = "Teste",
        startedAtMillis = millis,
        finishedAtMillis = millis + 1,
        effort = 1,
        sets = done.map {
            CompletedSet(
                exerciseId = "ex1",
                exerciseName = "Agachamento",
                muscleGroup = MuscleGroup.Pernas,
                loadKg = load,
                reps = it,
            )
        },
    )
}
