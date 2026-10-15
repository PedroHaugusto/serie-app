package com.serie.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.Radius
import com.serie.app.ui.theme.accentGlow

/**
 * Cabecalho de tela: titulo a esquerda, acao opcional a direita, e uma
 * navegacao de volta quando a tela nao e um destino de aba.
 */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    navIcon: PhIcon? = null,
    onNav: (() -> Unit)? = null,
    actionLabel: String? = null,
    actionIcon: PhIcon? = null,
    onAction: (() -> Unit)? = null,
) {
    val c = Nocturne.colors
    Row(
        modifier = modifier.fillMaxWidth().height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (navIcon != null && onNav != null) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onNav),
                contentAlignment = Alignment.Center,
            ) { PhIcon(navIcon, size = 20.dp, tint = c.text) }
        }
        Text(title, style = Nocturne.type.h1, color = c.text, modifier = Modifier.weight(1f))
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = Nocturne.type.button,
                color = c.accent,
                modifier = Modifier
                    .clip(Radius.md)
                    .clickable(onClick = onAction)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            )
        } else if (actionIcon != null && onAction != null) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onAction),
                contentAlignment = Alignment.Center,
            ) { PhIcon(actionIcon, size = 20.dp, tint = c.text) }
        }
    }
}

/** Rotulo de secao entre blocos. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, trailing: String? = null) {
    val c = Nocturne.colors
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = Nocturne.type.itemTitle, color = c.text)
        trailing?.let { Text(it, style = Nocturne.type.caption, color = c.textMuted) }
    }
}

/**
 * Metrica com icone, valor grande e legenda. Tres delas dividem a largura na
 * tela Hoje, separadas por fios verticais.
 */
@Composable
fun StatTile(
    icon: PhIcon,
    value: String,
    caption: String,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    tint: Color = Nocturne.colors.accent,
) {
    val c = Nocturne.colors
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        PhIcon(icon, size = 18.dp, tint = tint, filled = filled)
        Text(value, style = Nocturne.type.h2, color = c.text)
        Text(
            caption,
            style = Nocturne.type.caption,
            color = c.textMuted,
            textAlign = TextAlign.Center,
        )
    }
}

/** Fio vertical fino, usado entre metricas. */
@Composable
fun VerticalHairline(modifier: Modifier = Modifier, height: androidx.compose.ui.unit.Dp = 48.dp) {
    Box(
        modifier
            .size(width = Dimens.hairline, height = height)
            .background(Nocturne.colors.divider)
    )
}

/**
 * Estado de tela sem conteudo: icone num anel com brilho, uma frase e uma
 * acao. O prototipo usa o mesmo desenho para vazio e para erro - muda so o
 * icone e o texto.
 */
@Composable
fun EmptyState(
    icon: PhIcon,
    title: String,
    body: String,
    ctaLabel: String,
    onCta: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Nocturne.colors
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .size(72.dp)
                .accentGlow(RoundedCornerShape(50), alpha = 0.22f)
                .clip(RoundedCornerShape(50))
                .background(c.surface),
            contentAlignment = Alignment.Center,
        ) { PhIcon(icon, size = 28.dp, tint = c.accent) }
        Text(title, style = Nocturne.type.h2, color = c.text, textAlign = TextAlign.Center)
        Text(
            body,
            style = Nocturne.type.bodySmall,
            color = c.textSecondary,
            textAlign = TextAlign.Center,
        )
        SerieButton(ctaLabel, onCta)
    }
}

/** Linha de lista com conteudo a esquerda e um indicador a direita. */
@Composable
fun ListRow(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(Radius.lg)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .background(Nocturne.colors.surface)
            .padding(Dimens.cardPaddingTight),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        leading?.invoke()
        Box(Modifier.weight(1f)) { content() }
        trailing?.invoke()
    }
}

/**
 * O disco com a letra do treino. E a identidade visual de cada treino no app,
 * e fica acentuado quando aquele treino e o do dia.
 */
@Composable
fun WorkoutBadge(letter: String, highlighted: Boolean, modifier: Modifier = Modifier) {
    val c = Nocturne.colors
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(Radius.md)
            .background(if (highlighted) c.accentRamp.s900 else c.neutral.s800),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            letter,
            style = Nocturne.type.h2,
            color = if (highlighted) c.accent else c.neutral.s300,
        )
    }
}
