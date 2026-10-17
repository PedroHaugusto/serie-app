package com.serie.app.domain

import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * O CSV e a unica copia dos dados fora do aparelho, e quem o abre e uma
 * planilha brasileira. Um separador trocado ou um ponto decimal nao dao erro
 * visivel: o arquivo abre, com os numeros errados.
 */
class CsvExportTest {

    private val zone = ZoneId.of("America/Sao_Paulo")
    private lateinit var original: Locale

    @Before
    fun useEnglishDevice() {
        original = Locale.getDefault()
        Locale.setDefault(Locale.US)
    }

    @After
    fun restore() {
        Locale.setDefault(original)
    }

    @Test
    fun `sao tres arquivos, um por assunto`() {
        val names = documents().map { it.name }
        assertEquals(
            listOf("serie-treinos.csv", "serie-peso.csv", "serie-medidas.csv"),
            names,
        )
    }

    @Test
    fun `treinos saem com uma linha por serie e dados da sessao repetidos`() {
        val lines = fileNamed("serie-treinos.csv").lines()
        assertEquals(
            "Data;Início;Fim;Treino;Nome do treino;Exercício;Grupo;" +
                "Peso (kg);Repetições;Volume (kg)",
            lines[0],
        )
        assertEquals("15/10/2026;18:00;19:00;A;Peito;Supino;Peito;42,5;10;425", lines[1])
        assertEquals("15/10/2026;18:00;19:00;A;Peito;Supino;Peito;45;8;360", lines[2])
    }

    @Test
    fun `pesagens e medidas saem com data brasileira e virgula decimal`() {
        assertEquals("Data;Peso (kg)", fileNamed("serie-peso.csv").lines()[0])
        assertEquals("16/10/2026;82,4", fileNamed("serie-peso.csv").lines()[1])

        assertEquals("Data;Medida;Valor (cm)", fileNamed("serie-medidas.csv").lines()[0])
        assertEquals("16/10/2026;Cintura;82,5", fileNamed("serie-medidas.csv").lines()[1])
    }

    @Test
    fun `medida apagada ainda aparece pelo id em vez de sumir`() {
        // A medicao sobrevive ao tipo num export feito de estado antigo; sem
        // isto a linha sairia com a coluna do nome vazia.
        val documents = CsvExport.documents(
            history = emptyList(),
            weights = emptyList(),
            measureTypes = emptyList(),
            measureEntries = listOf(MeasureEntry("e1", "mt-coxa", day(16), 58.0)),
            zone = zone,
        )
        assertTrue(documents[2].content.contains("mt-coxa"))
    }

    @Test
    fun `nome com ponto e virgula ganha aspas para nao virar duas colunas`() {
        val documents = CsvExport.documents(
            history = listOf(
                session(
                    name = "Supino; reto",
                    sets = listOf(40.0 to 10),
                ),
            ),
            weights = emptyList(),
            measureTypes = emptyList(),
            measureEntries = emptyList(),
            zone = zone,
        )
        val line = documents[0].content.lines()[1]
        assertTrue(line, line.contains("\"Supino; reto\""))
        // Lido como a planilha le - respeitando as aspas - a linha tem as dez
        // colunas do cabecalho, e o ponto e virgula de dentro do nome nao
        // empurra as colunas seguintes.
        val fields = readCsvLine(line)
        assertEquals(10, fields.size)
        assertEquals("Supino; reto", fields[5])
    }

    /** Divisor de linha CSV que entende aspas, como qualquer leitor de verdade. */
    private fun readCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val field = StringBuilder()
        var quoted = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' && quoted && line.getOrNull(i + 1) == '"' -> {
                    field.append('"')
                    i++
                }
                ch == '"' -> quoted = !quoted
                ch == ';' && !quoted -> {
                    fields += field.toString()
                    field.clear()
                }
                else -> field.append(ch)
            }
            i++
        }
        fields += field.toString()
        return fields
    }

    @Test
    fun `arquivo vazio ainda traz o cabecalho`() {
        val documents = CsvExport.documents(emptyList(), emptyList(), emptyList(), emptyList(), zone)
        documents.forEach { assertEquals(1, it.content.trim().lines().size) }
    }

    // ------------------------------------------------------------- fixtures

    private fun documents() = CsvExport.documents(
        history = listOf(session(sets = listOf(42.5 to 10, 45.0 to 8))),
        weights = listOf(BodyWeight("w1", day(16), 82.4)),
        measureTypes = listOf(MeasureType("mt-cintura", "Cintura", lowerIsBetter = true)),
        measureEntries = listOf(MeasureEntry("e1", "mt-cintura", day(16), 82.5)),
        zone = zone,
    )

    private fun fileNamed(name: String) =
        documents().first { it.name == name }.content

    /** Milissegundos das 18:00 de um dia de outubro de 2026, em Sao Paulo. */
    private fun day(dayOfMonth: Int): Long =
        LocalDate.of(2026, 10, dayOfMonth)
            .atTime(18, 0)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

    private fun session(
        name: String = "Supino",
        sets: List<Pair<Double, Int>>,
    ) = CompletedSession(
        id = "s1",
        workoutId = "w-a",
        workoutLetter = "A",
        workoutName = "Peito",
        startedAtMillis = day(15),
        finishedAtMillis = day(15) + 3_600_000,
        effort = 1,
        sets = sets.map { (load, reps) ->
            CompletedSet(
                exerciseId = "ex1",
                exerciseName = name,
                muscleGroup = MuscleGroup.Peito,
                loadKg = load,
                reps = reps,
            )
        },
    )
}
