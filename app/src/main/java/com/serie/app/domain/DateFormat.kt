package com.serie.app.domain

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Datas em portugues, formatadas a mao.
 *
 * O `DateTimeFormatter` com locale pt-BR devolve "quinta-feira" e "qui.", e
 * nenhum dos dois e o que o design pede ("Quinta"). Em vez de brigar com os
 * dados de locale para extrair a forma do meio, as tabelas abaixo dizem
 * exatamente o que aparece na tela.
 */
object DateFormat {

    private val WEEKDAYS = listOf(
        "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo",
    )

    /** Iniciais da faixa da semana, de segunda a domingo. */
    val WEEKDAY_INITIALS = listOf("S", "T", "Q", "Q", "S", "S", "D")

    /** "Seg", "Ter"... para listar os dias do lembrete. */
    val WEEKDAY_SHORT = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")

    private val MONTHS = listOf(
        "janeiro", "fevereiro", "março", "abril", "maio", "junho",
        "julho", "agosto", "setembro", "outubro", "novembro", "dezembro",
    )

    private val MONTHS_SHORT = listOf(
        "jan", "fev", "mar", "abr", "mai", "jun",
        "jul", "ago", "set", "out", "nov", "dez",
    )

    /** "Quinta, 15 de outubro" - o cabecalho da tela Hoje. */
    fun longDate(date: LocalDate): String {
        val weekday = WEEKDAYS[date.dayOfWeek.value - 1]
        val month = MONTHS[date.monthValue - 1]
        return "$weekday, ${date.dayOfMonth} de $month"
    }

    /** "Quinta, 15 out" - o cabecalho do resumo. */
    fun shortDate(date: LocalDate): String {
        val weekday = WEEKDAYS[date.dayOfWeek.value - 1]
        val month = MONTHS_SHORT[date.monthValue - 1]
        return "$weekday, ${date.dayOfMonth} $month"
    }

    /** "17/10/2026" - o formato que uma planilha brasileira entende. */
    fun numericDate(date: LocalDate): String =
        "%02d/%02d/%04d".format(date.dayOfMonth, date.monthValue, date.year)

    /** "março de 2026" - o campo "treinando desde" do perfil. */
    fun monthYear(date: LocalDate): String =
        "${MONTHS[date.monthValue - 1]} de ${date.year}"

    /** "18:42" */
    fun time(dateTime: LocalDateTime): String =
        "%02d:%02d".format(dateTime.hour, dateTime.minute)

    /** "18:42 – 19:36" - o intervalo do treino concluido. */
    fun timeRange(startMillis: Long, endMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String {
        val start = LocalDateTime.ofInstant(Instant.ofEpochMilli(startMillis), zone)
        val end = LocalDateTime.ofInstant(Instant.ofEpochMilli(endMillis), zone)
        return "${time(start)} – ${time(end)}"
    }

    fun dateOf(millis: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
}
