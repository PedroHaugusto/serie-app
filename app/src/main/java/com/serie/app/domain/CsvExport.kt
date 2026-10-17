package com.serie.app.domain

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * O backup do app em CSV.
 *
 * Tres arquivos em vez de um: treinos, pesagens e medidas tem colunas
 * diferentes, e empilha-los num arquivo so obrigaria uma coluna "tipo" e
 * celulas vazias em quase toda linha - o que uma planilha nao sabe somar.
 *
 * O separador e ponto e virgula, nao virgula. O Excel em portugues le o CSV
 * pelo separador de lista do sistema, que no Brasil e ponto e virgula; com
 * virgula ele joga a linha inteira numa celula - e, pior, a virgula decimal
 * dos pesos quebraria cada numero em duas colunas.
 */
object CsvExport {

    private const val SEP = ";"

    /** Um arquivo pronto para gravar. */
    data class Document(val name: String, val content: String)

    fun documents(
        history: List<CompletedSession>,
        weights: List<BodyWeight>,
        measureTypes: List<MeasureType>,
        measureEntries: List<MeasureEntry>,
        zone: ZoneId = ZoneId.systemDefault(),
    ): List<Document> = listOf(
        Document("serie-treinos.csv", sessions(history, zone)),
        Document("serie-peso.csv", bodyWeights(weights)),
        Document("serie-medidas.csv", measures(measureTypes, measureEntries)),
    )

    /**
     * Uma linha por serie, com os dados da sessao repetidos.
     *
     * Repetir a data e o treino em cada linha e o que deixa a planilha
     * agrupar por exercicio ou por mes sem precisar casar duas tabelas.
     */
    private fun sessions(history: List<CompletedSession>, zone: ZoneId): String {
        val rows = history
            .sortedBy { it.startedAtMillis }
            .flatMap { session ->
                val date = DateFormat.numericDate(session.date)
                val start = DateFormat.time(at(session.startedAtMillis, zone))
                val end = DateFormat.time(at(session.finishedAtMillis, zone))
                session.sets.map { set ->
                    listOf(
                        date,
                        start,
                        end,
                        session.workoutLetter,
                        session.workoutName,
                        set.exerciseName,
                        set.muscleGroup.label,
                        Num.trimmed(set.loadKg),
                        set.reps.toString(),
                        Num.trimmed(set.loadKg * set.reps),
                    )
                }
            }
        return build(
            listOf(
                "Data", "Início", "Fim", "Treino", "Nome do treino", "Exercício",
                "Grupo", "Peso (kg)", "Repetições", "Volume (kg)",
            ),
            rows,
        )
    }

    private fun bodyWeights(weights: List<BodyWeight>): String = build(
        listOf("Data", "Peso (kg)"),
        weights.sortedBy { it.atMillis }.map {
            listOf(DateFormat.numericDate(it.date), Num.decimal(it.kg))
        },
    )

    private fun measures(types: List<MeasureType>, entries: List<MeasureEntry>): String {
        val nameOf = types.associate { it.id to it.name }
        return build(
            listOf("Data", "Medida", "Valor (cm)"),
            entries.sortedBy { it.atMillis }.map {
                listOf(
                    DateFormat.numericDate(it.date),
                    nameOf[it.typeId] ?: it.typeId,
                    Num.trimmed(it.cm),
                )
            },
        )
    }

    private fun at(millis: Long, zone: ZoneId): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), zone)

    private fun build(header: List<String>, rows: List<List<String>>): String =
        (listOf(header) + rows).joinToString("\r\n") { row ->
            row.joinToString(SEP) { escape(it) }
        } + "\r\n"

    /**
     * Um campo so ganha aspas quando precisa.
     *
     * Exercicios criados pelo usuario podem ter ponto e virgula ou aspas no
     * nome, e sem isso "Supino; reto" viraria duas colunas e desalinharia a
     * linha inteira.
     */
    private fun escape(field: String): String =
        if (field.any { it == ';' || it == '"' || it == '\n' || it == '\r' }) {
            "\"" + field.replace("\"", "\"\"") + "\""
        } else {
            field
        }
}
