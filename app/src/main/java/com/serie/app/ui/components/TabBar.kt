package com.serie.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.accentGlow

@Immutable
data class SerieTab(val id: String, val label: String, val icon: PhIcon)

/**
 * Barra de abas de 64 dp com quatro destinos.
 *
 * O destino ativo muda em tres eixos ao mesmo tempo - cor de acento, icone na
 * variante preenchida e um traco de 28 x 2 dp com brilho encostado no topo -
 * para que a selecao se leia sem depender apenas da cor.
 */
@Composable
fun SerieTabBar(
    tabs: List<SerieTab>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Nocturne.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.tabBarHeight)
            .background(c.surface),
    ) {
        tabs.forEach { tab ->
            val active = tab.id == selectedId
            val interaction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = { onSelect(tab.id) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (active) {
                    Box(
                        Modifier
                            .align(Alignment.TopCenter)
                            .width(Dimens.activeTabMarkWidth)
                            .height(Dimens.activeTabMark)
                            .accentGlow(RoundedCornerShape(2.dp), alpha = 0.55f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(c.accent)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    PhIcon(
                        icon = tab.icon,
                        size = 22.dp,
                        tint = if (active) c.accent else c.neutral.s400,
                        filled = active,
                    )
                    Text(
                        text = tab.label,
                        style = Nocturne.type.caption.copy(
                            fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                        ),
                        color = if (active) c.accent else c.neutral.s400,
                    )
                }
            }
        }
    }
}
