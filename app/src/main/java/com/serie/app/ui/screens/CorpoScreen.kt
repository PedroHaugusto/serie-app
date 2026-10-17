package com.serie.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.serie.app.domain.DateFormat
import com.serie.app.domain.MeasureRow
import com.serie.app.domain.plural
import com.serie.app.domain.Stats
import com.serie.app.ui.SerieUiState
import com.serie.app.ui.SerieViewModel
import com.serie.app.ui.components.Ph
import com.serie.app.ui.components.SerieBlockButton
import com.serie.app.ui.components.SerieButtonStyle
import com.serie.app.ui.components.ScreenHeader
import com.serie.app.ui.components.SectionLabel
import com.serie.app.ui.components.SerieCard
import com.serie.app.ui.components.SerieDivider
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne

/** 09 · Corpo e medidas. */
@Composable
fun CorpoScreen(
    state: SerieUiState,
    viewModel: SerieViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Nocturne.colors
    val latest = state.bodyWeights.maxByOrNull { it.atMillis }
    val change = Stats.weightChange(state.bodyWeights, state.today)
    val lastMeasured = state.measureRows.mapNotNull { it.latest }.maxByOrNull { it.atMillis }

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
                actionLabel = "Pesar",
                onAction = viewModel::openRecordWeight,
            )
        }

        item {
            SerieCard(padding = Dimens.cardPaddingTight) {
                Text("Peso corporal", style = Nocturne.type.caption, color = c.textMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        latest?.let { Stats.formatWeight(it.kg) } ?: "—",
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
                            change?.let { (delta, _) ->
                                (if (delta > 0) "+" else "−") +
                                    Stats.formatWeight(kotlin.math.abs(delta)) + " kg"
                            } ?: "—",
                            style = Nocturne.type.itemTitle,
                            color = c.accent,
                        )
                        Text(
                            change?.let { (_, days) -> "em " + plural(days.toInt(), "dia") }
                                ?: if (latest == null) "toque em Pesar" else "primeira pesagem",
                            style = Nocturne.type.caption,
                            color = c.textMuted,
                        )
                    }
                }
                Text(
                    latest?.let { "Registrado em " + DateFormat.shortDate(it.date) }
                        ?: "Nenhuma pesagem registrada.",
                    style = Nocturne.type.caption,
                    color = c.textMuted,
                )
            }
        }

        item {
            SectionLabel(
                "Medidas",
                trailing = lastMeasured
                    ?.let { "última em " + DateFormat.shortDate(it.date) }
                    ?: "nenhuma ainda",
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TableHead("Medida (cm)", Modifier.weight(1f), TextAlign.Start)
                    TableHead("Ant.", Modifier.width(52.dp), TextAlign.End)
                    TableHead("Atual", Modifier.width(52.dp), TextAlign.End)
                    TableHead("Δ", Modifier.width(52.dp), TextAlign.End)
                }
                SerieDivider(fade = 24.dp)
                state.measureRows.forEach { row ->
                    MeasureTableRow(row)
                    SerieDivider(fade = 24.dp)
                }
            }
        }

        item {
            SerieBlockButton(
                label = "Atualizar medidas",
                onClick = viewModel::openEditMeasures,
                style = SerieButtonStyle.Secondary,
                leadingIcon = Ph.plus,
                height = Dimens.touchTargetMin,
            )
        }
    }

    if (state.recordingWeight) {
        RegistrarPesoDialog(
            current = latest?.let { Stats.formatWeight(it.kg) }.orEmpty(),
            onDismiss = viewModel::dismissRecordWeight,
            onSave = viewModel::recordWeight,
        )
    }

    if (state.editingMeasures) {
        RegistrarMedidasDialog(
            rows = state.measureRows,
            onDismiss = viewModel::dismissEditMeasures,
            onSave = viewModel::saveMeasures,
            onCreate = viewModel::openCreateMeasure,
            onDelete = viewModel::deleteMeasure,
        )
    }

    if (state.creatingMeasure) {
        NovaMedidaDialog(
            onDismiss = viewModel::dismissCreateMeasure,
            onCreate = viewModel::createMeasure,
        )
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
private fun MeasureTableRow(row: MeasureRow) {
    val c = Nocturne.colors
    val delta = row.deltaCm
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(row.type.name, style = Nocturne.type.bodySmall, color = c.text)
            // O intervalo so aparece quando ha dois pontos: "+1,5 cm" sozinho
            // nao diz se foi num mes ou num ano.
            if (row.previous != null && row.latest != null) {
                Text(
                    DateFormat.shortDate(row.previous.date) + " → " +
                        DateFormat.shortDate(row.latest.date),
                    style = Nocturne.type.caption,
                    color = c.textMuted,
                )
            }
        }
        Cell(row.previous?.let { Stats.formatCm(it.cm) }, c.textMuted)
        Cell(row.latest?.let { Stats.formatCm(it.cm) }, c.text)
        Cell(
            text = delta?.let {
                (if (it > 0) "+" else if (it < 0) "−" else "") +
                    Stats.formatCm(kotlin.math.abs(it))
            },
            color = when (row.improved) {
                true -> c.accent
                false -> c.textSecondary
                null -> c.textMuted
            },
        )
    }
}

@Composable
private fun Cell(text: String?, color: androidx.compose.ui.graphics.Color) {
    Text(
        text ?: "—",
        style = Nocturne.type.bodySmall,
        color = if (text == null) Nocturne.colors.neutral.s500 else color,
        modifier = Modifier.width(52.dp),
        textAlign = TextAlign.End,
    )
}
