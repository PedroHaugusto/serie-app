package com.serie.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.Radius
import com.serie.app.ui.theme.accentGlow

/**
 * FAB estendido. Diferente do Material, nao e um bloco de cor: e superficie
 * com borda de acento e o mesmo halo da acao principal, para nao competir com
 * a CTA da tela nem furar o tema.
 */
@Composable
fun SerieExtendedFab(
    label: String,
    icon: PhIcon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Nocturne.colors
    Row(
        modifier = modifier
            .accentGlow(Radius.pill, alpha = 0.30f)
            .height(Dimens.touchTarget)
            .clip(Radius.pill)
            .background(c.surface)
            .border(Dimens.hairline, c.accent, Radius.pill)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PhIcon(icon, size = 18.dp, tint = c.accent)
        Text(label, style = Nocturne.type.button, color = c.accent)
    }
}
