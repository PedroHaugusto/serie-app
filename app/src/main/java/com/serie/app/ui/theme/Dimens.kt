package com.serie.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Metricas do handoff, medidas no frame de 360 x 760 dp. Sao numeros do
 * prototipo, nao escolhas livres: mexer aqui desalinha o app do design.
 */
object Dimens {
    /** Margem lateral da tela. */
    val screenMargin = 16.dp

    /** Respiro entre blocos de conteudo. */
    val blockGap = 14.dp
    val blockGapLarge = 16.dp

    /** Respiro interno de card. */
    val cardPadding = 16.dp
    val cardPaddingTight = 14.dp

    /** Alvo de toque minimo; a CTA principal usa o limite de cima. */
    val touchTargetMin = 44.dp
    val touchTarget = 48.dp

    /** Barra de abas e a faixa do gesto de navegacao do sistema. */
    val tabBarHeight = 64.dp
    val gestureBarHeight = 20.dp

    /** Espessura do fio de divisao e do traco da aba ativa. */
    val hairline = 1.dp
    val activeTabMark = 2.dp
    val activeTabMarkWidth = 28.dp

    /** Raio do brilho de acento. */
    val glowRadius = 24.dp
}

object Radius {
    val sm = RoundedCornerShape(4.dp)
    val md = RoundedCornerShape(8.dp)
    val lg = RoundedCornerShape(14.dp)
    /** O chip usa 3/4 do raio medio, como no .tag do design system. */
    val tag = RoundedCornerShape(6.dp)
    val pill = RoundedCornerShape(50)
}
