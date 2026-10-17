package com.serie.app.domain

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * O lembrete e a unica parte do app que acontece com ele fechado, e um erro
 * aqui nao da sinal: nao ha tela onde o alarme errado apareca, so a
 * notificacao que nao chega. Por isso a regra do proximo disparo e testada
 * aqui em vez de ser conferida no aparelho.
 */
class ReminderTest {

    /** Quinta-feira, 15 de outubro de 2026. */
    private val quinta = LocalDateTime.of(2026, 10, 15, 12, 0)

    @Test
    fun `sem dia marcado nao ha disparo`() {
        assertNull(Reminder.nextTrigger(prefs(days = emptySet()), quinta))
    }

    @Test
    fun `interruptor desligado nao dispara mesmo com dias marcados`() {
        val off = prefs(days = setOf(3), on = false)
        assertFalse(off.reminderActive)
        assertNull(Reminder.nextTrigger(off, quinta))
    }

    @Test
    fun `hoje antes da hora dispara hoje`() {
        // Quinta e o indice 3, e sao meio-dia: as 18h de hoje ainda vem.
        val next = Reminder.nextTrigger(prefs(days = setOf(3)), quinta)
        assertEquals(LocalDateTime.of(2026, 10, 15, 18, 0), next)
    }

    @Test
    fun `hoje depois da hora pula para o proximo dia marcado`() {
        val tarde = LocalDateTime.of(2026, 10, 15, 19, 30)
        val next = Reminder.nextTrigger(prefs(days = setOf(3, 4)), tarde)
        assertEquals(LocalDateTime.of(2026, 10, 16, 18, 0), next)
    }

    @Test
    fun `unico dia marcado e ja vencido volta na semana seguinte`() {
        // O caso que uma janela ate domingo perderia: sem achar nada ela
        // devolveria nulo, o alarme nao seria reagendado e o lembrete morreria
        // em silencio depois de soar uma vez.
        val tarde = LocalDateTime.of(2026, 10, 15, 19, 30)
        val next = Reminder.nextTrigger(prefs(days = setOf(3)), tarde)
        assertEquals(LocalDateTime.of(2026, 10, 22, 18, 0), next)
    }

    @Test
    fun `atravessa o domingo para chegar na segunda`() {
        val domingo = LocalDateTime.of(2026, 10, 18, 20, 0)
        val next = Reminder.nextTrigger(prefs(days = setOf(0)), domingo)
        assertEquals(LocalDateTime.of(2026, 10, 19, 18, 0), next)
    }

    @Test
    fun `o horario escolhido e respeitado`() {
        // Marcado so na sexta, as 6:30 - antes da hora atual, mas em outro dia.
        val next = Reminder.nextTrigger(prefs(days = setOf(4), hour = 6, minute = 30), quinta)
        assertEquals(LocalDateTime.of(2026, 10, 16, 6, 30), next)
    }

    @Test
    fun `o rotulo lista os dias marcados`() {
        assertEquals("Seg, Qua, Sex · 18:00", prefs(days = setOf(0, 2, 4)).reminderLabel)
        assertEquals("Todos os dias · 18:00", prefs(days = (0..6).toSet()).reminderLabel)
        assertEquals("Escolha os dias", prefs(days = emptySet()).reminderLabel)
        assertTrue(prefs(days = setOf(3), hour = 7, minute = 5).reminderLabel.endsWith("07:05"))
    }

    private fun prefs(
        days: Set<Int>,
        on: Boolean = true,
        hour: Int = 18,
        minute: Int = 0,
    ) = Preferences(
        darkTheme = true,
        remindersOn = on,
        reminderDays = List(7) { it in days },
        reminderHour = hour,
        reminderMinute = minute,
    )
}
