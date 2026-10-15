package com.serie.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.Radius

/**
 * Seletor segmentado.
 *
 * A opcao escolhida nao ganha fundo: ela recebe o acento no texto e um
 * contorno interno do acento, que no CSS original e um inset box-shadow.
 * Aqui isso vira uma borda desenhada sobre o recorte do grupo.
 */
@Composable
fun SerieSegmented(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    fillWidth: Boolean = false,
) {
    val c = Nocturne.colors
    Row(
        modifier = modifier
            .clip(Radius.md)
            .border(Dimens.hairline, c.divider, Radius.md)
            // Altura fixa, nao minima: os separadores internos usam
            // fillMaxHeight, e com altura so minima eles esticam ate o limite
            // vertical que o pai oferecer - dentro de uma Column solta, isso e
            // a tela inteira.
            .height(36.dp),
    ) {
        options.forEachIndexed { i, label ->
            if (i > 0) {
                Box(
                    Modifier
                        .width(Dimens.hairline)
                        .fillMaxHeight()
                        .background(c.divider)
                )
            }
            SegmentOption(
                label = label,
                selected = i == selectedIndex,
                fillWidth = fillWidth,
                onClick = { onSelect(i) },
            )
        }
    }
}

@Composable
private fun RowScope.SegmentOption(
    label: String,
    selected: Boolean,
    fillWidth: Boolean,
    onClick: () -> Unit,
) {
    val c = Nocturne.colors
    Box(
        modifier = Modifier
            .then(if (fillWidth) Modifier.weight(1f) else Modifier)
            .height(36.dp)
            .clickable(onClick = onClick)
            .then(if (selected) Modifier.border(Dimens.hairline, c.accent, Radius.md) else Modifier)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = Nocturne.type.segment,
            color = if (selected) c.accent else c.text,
        )
    }
}
