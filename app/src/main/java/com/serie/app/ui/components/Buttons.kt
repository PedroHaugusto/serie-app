package com.serie.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.LocalIndication
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.Radius
import com.serie.app.ui.theme.accentGlow

/**
 * Botoes do Nocturne. Nenhum deles e preenchido: a acao principal se destaca
 * por contorno no acento mais um halo do mesmo acento, nao por um bloco solido.
 * E uma decisao do design system - preencher quebraria a leitura do tema.
 */

enum class SerieButtonStyle { Primary, Secondary, Ghost }

@Composable
fun SerieButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: SerieButtonStyle = SerieButtonStyle.Primary,
    enabled: Boolean = true,
    leadingIcon: PhIcon? = null,
    leadingIconFilled: Boolean = false,
    trailingIcon: PhIcon? = null,
    height: Dp = Dimens.touchTarget,
    glow: Boolean = style == SerieButtonStyle.Primary,
    /** Afasta rotulo e icone final para as pontas, como na CTA de "Iniciar treino". */
    spread: Boolean = false,
) {
    val c = Nocturne.colors
    val content = when (style) {
        SerieButtonStyle.Primary -> c.accent
        SerieButtonStyle.Secondary -> c.text
        SerieButtonStyle.Ghost -> c.accent
    }
    val border = when (style) {
        SerieButtonStyle.Primary -> c.accent
        SerieButtonStyle.Secondary -> c.divider
        SerieButtonStyle.Ghost -> Color.Transparent
    }
    val shape = Radius.md
    val interaction = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .then(if (glow && enabled) Modifier.accentGlow(shape, alpha = 0.30f) else Modifier)
            .height(height)
            .clip(shape)
            .border(BorderStroke(Dimens.hairline, border), shape)
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = LocalIndication.current,
                onClick = onClick,
            )
            .padding(horizontal = if (style == SerieButtonStyle.Ghost) 4.dp else 16.dp)
            .alpha(if (enabled) 1f else 0.45f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement =
            if (spread) Arrangement.SpaceBetween else Arrangement.spacedBy(8.dp),
    ) {
        if (spread) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ButtonContent(leadingIcon, leadingIconFilled, label, content)
            }
            trailingIcon?.let { PhIcon(it, size = 18.dp, tint = content) }
        } else {
            ButtonContent(leadingIcon, leadingIconFilled, label, content)
            trailingIcon?.let { PhIcon(it, size = 18.dp, tint = content) }
        }
    }
}

@Composable
private fun RowScope.ButtonContent(
    icon: PhIcon?,
    filled: Boolean,
    label: String,
    color: Color,
) {
    icon?.let { PhIcon(it, size = 18.dp, tint = color, filled = filled) }
    Text(
        text = label,
        style = Nocturne.type.button,
        color = color,
    )
}

/** Botao que ocupa a largura toda - o padrao das CTAs de tela. */
@Composable
fun SerieBlockButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: SerieButtonStyle = SerieButtonStyle.Primary,
    enabled: Boolean = true,
    leadingIcon: PhIcon? = null,
    leadingIconFilled: Boolean = false,
    trailingIcon: PhIcon? = null,
    height: Dp = Dimens.touchTarget,
    glow: Boolean = style == SerieButtonStyle.Primary,
    spread: Boolean = false,
) = SerieButton(
    label = label,
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
    style = style,
    enabled = enabled,
    leadingIcon = leadingIcon,
    leadingIconFilled = leadingIconFilled,
    trailingIcon = trailingIcon,
    height = height,
    glow = glow,
    spread = spread,
)

/** Botao quadrado so com icone, usado em cabecalhos. */
@Composable
fun SerieIconButton(
    icon: PhIcon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Nocturne.colors.text,
    size: Dp = 40.dp,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.padding(8.dp)) { PhIcon(icon, size = size - 20.dp, tint = tint) }
    }
}
