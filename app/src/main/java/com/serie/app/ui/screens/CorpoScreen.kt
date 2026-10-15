package com.serie.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.serie.app.data.SeedData
import com.serie.app.domain.BodyMeasure
import com.serie.app.ui.components.Ph
import com.serie.app.ui.components.PhIcon
import com.serie.app.ui.components.ScreenHeader
import com.serie.app.ui.components.SectionLabel
import com.serie.app.ui.components.SerieCard
import com.serie.app.ui.components.SerieDivider
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.Radius
import com.serie.app.ui.theme.elevSm

/** 09 · Corpo e medidas. */
@Composable
fun CorpoScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val c = Nocturne.colors

    LazyColumn(
        modifier = modifier.fillMaxSize().background(c.bg),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = Dimens.screenMargin,
            end = Dimens.screenMargin,
            bottom = Dimens.blockGapLarge,
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.blockGap),
    ) {
        item {
            ScreenHeader(
                title = "Corpo",
                navIcon = Ph.arrowLeft,
                onNav = onBack,
                actionLabel = "Registrar",
                onAction = {},
            )
        }

        item {
            SerieCard(padding = Dimens.cardPaddingTight) {
                Text("Peso corporal", style = Nocturne.type.caption, color = c.textMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        SeedData.BODY_WEIGHT,
                        style = Nocturne.type.display,
                        color = c.text,
                        modifier = Modifier.alignByBaseline(),
                    )
                    Text(
                        "kg",
                        style = Nocturne.type.h2,
                        color = c.textMuted,
                        modifier = Modifier.alignByBaseline(),
                    )
                    Column(
                        Modifier.padding(start = 10.dp).alignByBaseline(),
                    ) {
                        Text(
                            SeedData.BODY_WEIGHT_DELTA,
                            style = Nocturne.type.itemTitle,
                            color = c.accent,
                        )
                        Text(
                            SeedData.BODY_WEIGHT_PERIOD,
                            style = Nocturne.type.caption,
                            color = c.textMuted,
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    PhIcon(Ph.heartbeat, size = 14.dp, tint = c.accent2)
                    Text(
                        SeedData.HEALTH_SYNC,
                        style = Nocturne.type.caption,
                        color = c.textMuted,
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TableHead("Medida (cm)", Modifier.weight(1f), TextAlign.Start)
                    TableHead("Jul", Modifier.width(52.dp), TextAlign.End)
                    TableHead("Out", Modifier.width(52.dp), TextAlign.End)
                    TableHead("Δ", Modifier.width(52.dp), TextAlign.End)
                }
                SerieDivider(fade = 24.dp)
                SeedData.measures.forEach { measure ->
                    MeasureRow(measure)
                    SerieDivider(fade = 24.dp)
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionLabel("Fotos de progresso", trailing = "Comparar")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SeedData.progressPhotos.forEach { label ->
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.75f)
                                    .clip(Radius.lg)
                                    .background(c.surface)
                                    .elevSm(Radius.lg),
                                contentAlignment = Alignment.Center,
                            ) {
                                PhIcon(Ph.image, size = 26.dp, tint = c.neutral.s500)
                            }
                            Text(label, style = Nocturne.type.caption, color = c.textMuted)
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    PhIcon(Ph.lockSimple, size = 13.dp, tint = c.textMuted)
                    Text(
                        "Fotos ficam só no seu aparelho.",
                        style = Nocturne.type.caption,
                        color = c.textMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun TableHead(text: String, modifier: Modifier, align: TextAlign) {
    Text(
        text,
        style = Nocturne.type.kicker,
        color = Nocturne.colors.textMuted,
        modifier = modifier,
        textAlign = align,
    )
}

@Composable
private fun MeasureRow(measure: BodyMeasure) {
    val c = Nocturne.colors
    // Encolher a cintura e ganho; encolher o braco nao. O sinal sozinho nao
    // diz se a variacao foi boa, entao a cor vem da intencao da medida.
    val improved = if (measure.lowerIsBetter) {
        measure.delta.startsWith("−")
    } else {
        measure.delta.startsWith("+")
    }
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(measure.name, style = Nocturne.type.bodySmall, color = c.text, modifier = Modifier.weight(1f))
        Text(
            measure.before,
            style = Nocturne.type.bodySmall,
            color = c.textMuted,
            modifier = Modifier.width(52.dp),
            textAlign = TextAlign.End,
        )
        Text(
            measure.after,
            style = Nocturne.type.bodySmall,
            color = c.text,
            modifier = Modifier.width(52.dp),
            textAlign = TextAlign.End,
        )
        Text(
            measure.delta,
            style = Nocturne.type.bodySmall,
            color = if (improved) c.accent else c.textSecondary,
            modifier = Modifier.width(52.dp),
            textAlign = TextAlign.End,
        )
    }
}
