package com.serie.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.Radius
import com.serie.app.ui.theme.elevSm

/**
 * Card do Nocturne: superficie, raio grande e o fio de contorno da elevacao 1.
 *
 * O [halo] e o brilho radial que o prototipo usa no card de destaque - um
 * gradiente de acento ancorado no canto superior direito. Fica desligado por
 * padrao: se todo card brilhar, nenhum destaca.
 */
@Composable
fun SerieCard(
    modifier: Modifier = Modifier,
    shape: Shape = Radius.lg,
    padding: Dp = Dimens.cardPadding,
    halo: Boolean = false,
    background: Color = Nocturne.colors.surface,
    gap: Dp = 10.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val accent = Nocturne.colors.accent
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(background)
            .then(
                if (halo) Modifier.drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = 0.16f), Color.Transparent),
                            center = Offset(size.width, 0f),
                            radius = size.width * 0.9f,
                        )
                    )
                } else Modifier
            )
            .elevSm(shape)
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(gap),
        content = content,
    )
}
