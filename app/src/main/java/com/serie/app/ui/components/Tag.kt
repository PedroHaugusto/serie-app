package com.serie.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.Radius

/**
 * Chip de rotulo. As variantes preenchidas usam o passo 800 da rampa como
 * fundo e o 100 como texto - e como as rampas se invertem entre os temas, o
 * mesmo par funciona no escuro e no claro sem condicional.
 */
enum class SerieTagStyle { Neutral, Accent, Accent2, Outline }

@Composable
fun SerieTag(
    label: String,
    modifier: Modifier = Modifier,
    style: SerieTagStyle = SerieTagStyle.Neutral,
    icon: PhIcon? = null,
) {
    val c = Nocturne.colors
    val bg: Color
    val fg: Color
    var outline = Color.Transparent
    when (style) {
        SerieTagStyle.Neutral -> { bg = c.neutral.s800; fg = c.neutral.s100 }
        SerieTagStyle.Accent -> { bg = c.accentRamp.s800; fg = c.accentRamp.s100 }
        SerieTagStyle.Accent2 -> { bg = c.accent2Ramp.s800; fg = c.accent2Ramp.s100 }
        SerieTagStyle.Outline -> { bg = Color.Transparent; fg = c.accent; outline = c.accent }
    }

    Row(
        modifier = modifier
            .clip(Radius.tag)
            .background(bg)
            .border(Dimens.hairline, outline, Radius.tag)
            .padding(horizontal = 10.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        icon?.let { PhIcon(it, size = 12.dp, tint = fg) }
        Text(label, style = Nocturne.type.caption, color = fg)
    }
}
