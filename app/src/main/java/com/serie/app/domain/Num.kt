package com.serie.app.domain

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Numeros no padrao brasileiro: virgula decimal e ponto nos milhares.
 *
 * Os simbolos vem fixos de pt-BR, e nao do locale do aparelho. O app escreve
 * em portugues de qualquer forma, e o codigo antigo formatava com o locale do
 * sistema e depois trocava ponto por virgula - o que acerta num telefone em
 * portugues e, num em ingles, transforma o separador de milhar em casa
 * decimal, fazendo "12.450 kg" virar "12,450 kg".
 */
object Num {

    private val SYMBOLS = DecimalFormatSymbols(Locale("pt", "BR"))

    /** "12.450" */
    fun integer(value: Double): String = format("#,##0", value)

    fun integer(value: Int): String = integer(value.toDouble())

    /** "78,4" - sempre com uma casa, inclusive "78,0". */
    fun decimal(value: Double): String = format("#,##0.0", value)

    /** "37,5" e "37" - a casa decimal so aparece quando existe. */
    fun trimmed(value: Double): String = format("#,##0.#", value)

    // DecimalFormat nao e seguro entre threads, e formatar peso acontece tanto
    // na composicao quanto dentro de corrotina. Um por chamada custa menos que
    // um numero errado sob concorrencia.
    private fun format(pattern: String, value: Double): String =
        DecimalFormat(pattern, SYMBOLS).format(value)
}
