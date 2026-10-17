package com.serie.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A tabela de medidas compara duas datas, e e nela que a tela decide o que
 * pintar como ganho. Testar aqui cobre o caso que o app nao consegue produzir
 * em uma sessao: duas medicoes em dias diferentes.
 */
class MeasureTest {

    private val braco = MeasureType("mt-braco", "Braço")
    private val cintura = MeasureType("mt-cintura", "Cintura", lowerIsBetter = true)

    @Test
    fun `pega a ultima medicao e a anterior a ela`() {
        val rows = Stats.measureRows(
            types = listOf(braco),
            entries = listOf(
                entry(braco, day = 1, cm = 36.0),
                entry(braco, day = 3, cm = 37.5),
                entry(braco, day = 2, cm = 37.0),
            ),
        )
        val row = rows.single()
        assertEquals(37.5, row.latest!!.cm, 0.001)
        assertEquals(37.0, row.previous!!.cm, 0.001)
        assertEquals(0.5, row.deltaCm!!, 0.001)
    }

    @Test
    fun `medida sem medicao nenhuma continua na tabela`() {
        // Some-la esconderia justamente o que falta medir.
        val row = Stats.measureRows(listOf(braco), emptyList()).single()
        assertNull(row.latest)
        assertNull(row.previous)
        assertNull(row.deltaCm)
        assertNull(row.improved)
    }

    @Test
    fun `uma medicao so nao produz variacao`() {
        val row = Stats.measureRows(listOf(braco), listOf(entry(braco, 1, 36.0))).single()
        assertEquals(36.0, row.latest!!.cm, 0.001)
        assertNull(row.previous)
        assertNull(row.deltaCm)
    }

    @Test
    fun `crescer e ganho no braco e perda na cintura`() {
        val cresceu = listOf(entry(braco, 1, 36.0), entry(braco, 2, 37.5))
        assertEquals(true, Stats.measureRows(listOf(braco), cresceu).single().improved)

        val cinturaCresceu = listOf(entry(cintura, 1, 82.0), entry(cintura, 2, 85.0))
        assertEquals(false, Stats.measureRows(listOf(cintura), cinturaCresceu).single().improved)
    }

    @Test
    fun `encolher e perda no braco e ganho na cintura`() {
        val encolheu = listOf(entry(braco, 1, 37.5), entry(braco, 2, 36.0))
        assertEquals(false, Stats.measureRows(listOf(braco), encolheu).single().improved)

        val cinturaEncolheu = listOf(entry(cintura, 1, 86.0), entry(cintura, 2, 82.5))
        assertEquals(true, Stats.measureRows(listOf(cintura), cinturaEncolheu).single().improved)
    }

    @Test
    fun `ficar igual nao e ganho nem perda`() {
        val igual = listOf(entry(braco, 1, 37.0), entry(braco, 2, 37.0))
        val row = Stats.measureRows(listOf(braco), igual).single()
        assertEquals(0.0, row.deltaCm!!, 0.001)
        assertNull(row.improved)
    }

    @Test
    fun `cada medida so enxerga as proprias medicoes`() {
        val rows = Stats.measureRows(
            types = listOf(braco, cintura),
            entries = listOf(entry(braco, 1, 36.0), entry(cintura, 2, 82.0)),
        )
        assertEquals(36.0, rows[0].latest!!.cm, 0.001)
        assertEquals(82.0, rows[1].latest!!.cm, 0.001)
        assertTrue(rows.all { it.previous == null })
    }

    @Test
    fun `a tabela segue a ordem das medidas, nao a das medicoes`() {
        val rows = Stats.measureRows(
            types = listOf(braco, cintura),
            entries = listOf(entry(cintura, 9, 82.0), entry(braco, 1, 36.0)),
        )
        assertEquals(listOf("Braço", "Cintura"), rows.map { it.type.name })
    }

    private fun entry(type: MeasureType, day: Int, cm: Double) = MeasureEntry(
        id = "${type.id}-$day",
        typeId = type.id,
        atMillis = day * 86_400_000L,
        cm = cm,
    )
}
