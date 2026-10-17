package com.serie.app.domain

/**
 * Plural e concordancia em portugues para as contagens da tela.
 *
 * Cada tela montava a contagem na mao, e o app mostrava "1 séries" no
 * historico, "1 pesagens" no perfil e "1 treinos registrados" na evolucao. A
 * regra do "m" esta aqui porque e justamente onde somar um "s" erra: o plural
 * de pesagem e pesagens, nao "pesagems".
 */
fun plural(count: Int, singular: String): String = "$count ${agree(count, singular)}"

/** A palavra no numero certo, sem a contagem na frente. */
fun agree(count: Int, word: String): String = when {
    count == 1 -> word
    word.endsWith("m") -> word.dropLast(1) + "ns"
    else -> word + "s"
}
