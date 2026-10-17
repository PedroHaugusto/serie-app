package com.serie.app.domain

import java.util.Locale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * O app escreve numero brasileiro em qualquer aparelho.
 *
 * O codigo antigo formatava com o locale do sistema e trocava ponto por
 * virgula depois, o que num telefone em ingles transformava o separador de
 * milhar em casa decimal. Cada teste aqui roda com o locale padrao em en-US
 * justamente para provar que o resultado nao depende dele.
 */
class NumTest {

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
    fun `milhar leva ponto`() {
        assertEquals("12.450", Num.integer(12_450.0))
        assertEquals("1.234.567", Num.integer(1_234_567))
        assertEquals("840", Num.integer(840.0))
    }

    @Test
    fun `decimal leva virgula e uma casa sempre`() {
        assertEquals("78,4", Num.decimal(78.44))
        assertEquals("78,0", Num.decimal(78.0))
        assertEquals("1.078,5", Num.decimal(1078.45))
    }

    @Test
    fun `trimmed esconde a casa decimal quando ela e zero`() {
        assertEquals("37", Num.trimmed(37.0))
        assertEquals("37,5", Num.trimmed(37.5))
        assertEquals("1.000", Num.trimmed(1000.0))
    }

    @Test
    fun `os formatos do app saem em padrao brasileiro`() {
        assertEquals("42,5 kg", Stats.formatKg(42.5))
        assertEquals("40 kg", Stats.formatKg(40.0))
        assertEquals("78,4", Stats.formatWeight(78.4))
        assertEquals("82", Stats.formatCm(82.0))
        assertEquals("82,5", Stats.formatCm(82.5))
    }

    @Test
    fun `volume vira tonelada acima de mil quilos`() {
        assertEquals("840 kg", Stats.formatVolume(840.0))
        assertEquals("999 kg", Stats.formatVolume(999.0))
        assertEquals("18,4 t", Stats.formatVolume(18_430.0))
    }
}
