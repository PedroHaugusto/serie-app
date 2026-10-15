package com.serie.app.ui.theme

import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Elevacao do Nocturne: contorno de 1 px sempre, sombra ambiente so a partir
 * do nivel medio. Num fundo escuro a sombra preta quase nao aparece - quem
 * separa o card do fundo e o contorno, nao o borrao.
 *
 * As cores de sombra (`ambientColor`/`spotColor`) so valem da API 28 em
 * diante; abaixo disso o Android pinta preto. Como o contorno carrega a
 * leitura de elevacao, o resultado degrada sem quebrar.
 */

/** Nivel 1: so o fio de contorno. */
@Composable
fun Modifier.elevSm(shape: Shape): Modifier = composed {
    border(Dimens.hairline, Nocturne.colors.shadowHairline, shape)
}

/** Nivel 2: contorno mais claro e sombra ambiente. */
@Composable
fun Modifier.elevMd(shape: Shape): Modifier = composed {
    val c = Nocturne.colors
    shadow(6.dp, shape, clip = false, ambientColor = c.shadowAmbient, spotColor = c.shadowAmbient)
        .border(Dimens.hairline, c.neutral.s700, shape)
}

/** Nivel 3: usado por dialogos e folhas. */
@Composable
fun Modifier.elevLg(shape: Shape): Modifier = composed {
    val c = Nocturne.colors
    shadow(16.dp, shape, clip = false, ambientColor = c.shadowAmbient, spotColor = c.shadowAmbient)
        .border(Dimens.hairline, c.neutral.s500, shape)
}

/**
 * Brilho de acento: a assinatura do Nocturne na acao principal e na aba ativa.
 * O prototipo pede `0 0 24px` com o acento entre 28% e 35%.
 */
@Composable
fun Modifier.accentGlow(
    shape: Shape,
    alpha: Float = 0.30f,
    color: Color = Nocturne.colors.accent,
): Modifier = composed {
    val glow = color.copy(alpha = alpha)
    shadow(Dimens.glowRadius / 2, shape, clip = false, ambientColor = glow, spotColor = glow)
}
