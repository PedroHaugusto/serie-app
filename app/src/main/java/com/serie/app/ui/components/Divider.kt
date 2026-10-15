package com.serie.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne

/**
 * Fio de divisao. A assinatura do Nocturne e que a regra livre desaparece nas
 * pontas em vez de terminar seca - a rampa de fade tem [fade] de cada lado.
 * Contornos de caixa e separadores internos de controle continuam solidos.
 */
@Composable
fun SerieDivider(
    modifier: Modifier = Modifier,
    color: Color = Nocturne.colors.divider,
    fade: Dp = 48.dp,
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(Dimens.hairline)
            .drawBehind {
                val f = (fade.toPx() / size.width).coerceIn(0f, 0.5f)
                drawRect(
                    Brush.horizontalGradient(
                        0f to Color.Transparent,
                        f to color,
                        (1f - f) to color,
                        1f to Color.Transparent,
                    )
                )
            }
    )
}
