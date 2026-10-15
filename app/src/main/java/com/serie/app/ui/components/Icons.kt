package com.serie.app.ui.components

import androidx.compose.runtime.Immutable

/**
 * Icones Phosphor - o mesmo set do prototipo.
 *
 * O Phosphor e uma fonte de icones: cada icone e um glifo num ponto de codigo
 * da area de uso privado, e as variantes regular e preenchida compartilham o
 * mesmo ponto em arquivos diferentes. Por isso basta um codigo por icone, e a
 * variante vira escolha de familia na hora de desenhar.
 *
 * Gerado a partir do CSS do Phosphor 2.1.1 embutido no prototipo, restrito aos
 * icones que as telas usam.
 */
@Immutable
@JvmInline
value class PhIcon(val codePoint: Int) {
    val glyph: String get() = String(Character.toChars(codePoint))
}

object Ph {
    /** ph-arrow-left */
    val arrowLeft = PhIcon(0xe058)
    /** ph-arrow-right */
    val arrowRight = PhIcon(0xe06c)
    /** ph-barbell */
    val barbell = PhIcon(0xe0b6)
    /** ph-battery-high */
    val batteryHigh = PhIcon(0xe0c2)
    /** ph-bell */
    val bell = PhIcon(0xe0ce)
    /** ph-caret-down */
    val caretDown = PhIcon(0xe136)
    /** ph-caret-right */
    val caretRight = PhIcon(0xe13a)
    /** ph-caret-up */
    val caretUp = PhIcon(0xe13c)
    /** ph-cell-signal-full */
    val cellSignalFull = PhIcon(0xe142)
    /** ph-chart-line-up */
    val chartLineUp = PhIcon(0xe156)
    /** ph-check */
    val check = PhIcon(0xe182)
    /** ph-cloud-slash */
    val cloudSlash = PhIcon(0xe1b6)
    /** ph-dots-six-vertical */
    val dotsSixVertical = PhIcon(0xeae2)
    /** ph-dots-three-vertical */
    val dotsThreeVertical = PhIcon(0xe208)
    /** ph-envelope-simple */
    val envelopeSimple = PhIcon(0xe218)
    /** ph-export */
    val export = PhIcon(0xeaf0)
    /** ph-flame */
    val flame = PhIcon(0xe624)
    /** ph-google-logo */
    val googleLogo = PhIcon(0xe292)
    /** ph-heartbeat */
    val heartbeat = PhIcon(0xe2ac)
    /** ph-house */
    val house = PhIcon(0xe2c2)
    /** ph-image */
    val image = PhIcon(0xe2ca)
    /** ph-lock-simple */
    val lockSimple = PhIcon(0xe308)
    /** ph-magnifying-glass */
    val magnifyingGlass = PhIcon(0xe30c)
    /** ph-moon */
    val moon = PhIcon(0xe330)
    /** ph-moon-stars */
    val moonStars = PhIcon(0xe58e)
    /** ph-person-simple */
    val personSimple = PhIcon(0xe72e)
    /** ph-person-simple-walk */
    val personSimpleWalk = PhIcon(0xe73a)
    /** ph-play */
    val play = PhIcon(0xe3d0)
    /** ph-play-circle */
    val playCircle = PhIcon(0xe3d2)
    /** ph-plus */
    val plus = PhIcon(0xe3d4)
    /** ph-ruler */
    val ruler = PhIcon(0xe6b8)
    /** ph-scales */
    val scales = PhIcon(0xe750)
    /** ph-share-network */
    val shareNetwork = PhIcon(0xe408)
    /** ph-sign-out */
    val signOut = PhIcon(0xe42a)
    /** ph-stack */
    val stack = PhIcon(0xe466)
    /** ph-timer */
    val timer = PhIcon(0xe492)
    /** ph-trash */
    val trash = PhIcon(0xe4a6)
    /** ph-trend-up */
    val trendUp = PhIcon(0xe4ae)
    /** ph-trophy */
    val trophy = PhIcon(0xe67e)
    /** ph-user */
    val user = PhIcon(0xe4c2)
    /** ph-warning-circle */
    val warningCircle = PhIcon(0xe4e2)
    /** ph-watch */
    val watch = PhIcon(0xe4e6)
    /** ph-wifi-high */
    val wifiHigh = PhIcon(0xe4ea)
    /** ph-x */
    val x = PhIcon(0xe4f6)
}
