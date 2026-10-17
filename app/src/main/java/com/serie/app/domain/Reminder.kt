package com.serie.app.domain

import java.time.LocalDateTime

/**
 * Quando o lembrete de treino deve soar.
 *
 * Fica separado do agendador do Android para que a regra - "o proximo dia
 * marcado, e se hoje ja passou da hora, o da semana que vem" - possa ser
 * testada sem aparelho. O AlarmManager so recebe o instante calculado aqui.
 */
object Reminder {

    /**
     * O proximo disparo depois de [from], ou nulo quando nao ha lembrete.
     *
     * A janela vai de hoje a sete dias a frente, e nao de hoje a domingo:
     * com um unico dia marcado e a hora de hoje ja vencida, parar no fim da
     * semana devolveria nulo e o lembrete simplesmente nunca mais soaria.
     */
    fun nextTrigger(preferences: Preferences, from: LocalDateTime): LocalDateTime? {
        if (!preferences.reminderActive) return null
        for (offset in 0L..7L) {
            val date = from.toLocalDate().plusDays(offset)
            val marked = preferences.reminderDays.getOrNull(date.dayOfWeek.value - 1) == true
            if (!marked) continue
            val candidate = date.atTime(preferences.reminderHour, preferences.reminderMinute)
            if (candidate.isAfter(from)) return candidate
        }
        return null
    }
}
