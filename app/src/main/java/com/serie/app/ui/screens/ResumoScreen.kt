package com.serie.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.serie.app.ui.SerieUiState
import com.serie.app.ui.SerieViewModel
import com.serie.app.ui.components.ListRow
import com.serie.app.ui.components.Ph
import com.serie.app.ui.components.PhIcon
import com.serie.app.ui.components.ScreenHeader
import com.serie.app.ui.components.SectionLabel
import com.serie.app.ui.components.SerieBlockButton
import com.serie.app.ui.components.SerieButtonStyle
import com.serie.app.ui.components.SerieCard
import com.serie.app.ui.components.SerieSegmented
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.Radius

/** 07 · Resumo pos-treino. */
@Composable
fun ResumoScreen(
    state: SerieUiState,
    viewModel: SerieViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Nocturne.colors
    val summary = state.summary ?: return

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
            ScreenHeader(title = "", navIcon = Ph.x, onNav = onClose)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("TREINO CONCLUÍDO", style = Nocturne.type.kicker, color = c.accentRamp.s300)
                Text(summary.workoutName, style = Nocturne.type.h1, color = c.text)
                Text(
                    "${summary.dateLabel} · ${summary.timeRange}",
                    style = Nocturne.type.caption,
                    color = c.textMuted,
                )
            }
        }

        item {
            SerieCard(padding = Dimens.cardPaddingTight) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SummaryStat(summary.durationLabel, "duração", Modifier.weight(1f))
                    SummaryStat(
                        "${"%,d".format(summary.volumeKg).replace(',', '.')} kg",
                        summary.volumeDelta,
                        Modifier.weight(1f),
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SummaryStat("${summary.totalSets}", "séries", Modifier.weight(1f))
                    SummaryStat("${summary.records.size}", "recordes", Modifier.weight(1f))
                }
            }
        }

        if (summary.records.isNotEmpty()) {
            item { SectionLabel("Recordes de hoje") }
            summary.records.forEach { record ->
                item {
                    ListRow(
                        leading = {
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .clip(Radius.md)
                                    .background(c.accentRamp.s800),
                                contentAlignment = Alignment.Center,
                            ) {
                                PhIcon(Ph.trophy, size = 18.dp, tint = c.accent, filled = true)
                            }
                        },
                    ) {
                        Column {
                            Text(
                                record.exerciseName,
                                style = Nocturne.type.itemTitle,
                                color = c.text,
                            )
                            Text(
                                record.detail,
                                style = Nocturne.type.caption,
                                color = c.textSecondary,
                            )
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionLabel("Como foi o esforço?")
                SerieSegmented(
                    options = listOf("Leve", "Ideal", "Pesado"),
                    selectedIndex = state.effort,
                    onSelect = viewModel::setEffort,
                    fillWidth = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SerieBlockButton(label = "Concluir", onClick = onClose)
                SerieBlockButton(
                    label = "Compartilhar treino",
                    onClick = {},
                    style = SerieButtonStyle.Secondary,
                    leadingIcon = Ph.shareNetwork,
                    height = Dimens.touchTargetMin,
                )
            }
        }
    }
}

@Composable
private fun SummaryStat(value: String, caption: String, modifier: Modifier = Modifier) {
    val c = Nocturne.colors
    Column(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(value, style = Nocturne.type.h2, color = c.text)
        Text(caption, style = Nocturne.type.caption, color = c.textMuted, textAlign = TextAlign.Center)
    }
}
