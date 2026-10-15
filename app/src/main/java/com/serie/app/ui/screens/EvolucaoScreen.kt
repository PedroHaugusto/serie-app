package com.serie.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.serie.app.domain.Stats
import com.serie.app.domain.ExerciseProgress
import com.serie.app.ui.SerieUiState
import com.serie.app.ui.SerieViewModel
import com.serie.app.ui.components.Ph
import com.serie.app.ui.components.PhIcon
import com.serie.app.ui.components.EmptyState
import com.serie.app.ui.components.ListRow
import com.serie.app.ui.components.ScreenHeader
import com.serie.app.ui.components.SectionLabel
import com.serie.app.ui.components.SerieCard
import com.serie.app.ui.components.SerieSegmented
import com.serie.app.ui.components.SerieTag
import com.serie.app.ui.components.SerieTagStyle
import com.serie.app.ui.components.VerticalHairline
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.accentGlow
import androidx.compose.foundation.Canvas

/** 08 · Evolucao - volume, carga e recordes ao longo do tempo. */
@Composable
fun EvolucaoScreen(
    state: SerieUiState,
    viewModel: SerieViewModel,
    onOpenBody: () -> Unit,
    onStartWorkout: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Nocturne.colors
    val period = state.volumeChart
    val progress = state.progress

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
                title = "Evolução",
                actionLabel = "Corpo",
                onAction = onOpenBody,
            )
        }

        if (!state.hasEnoughHistory) {
            item {
                EmptyState(
                    icon = Ph.chartLineUp,
                    title = "Poucos dados ainda",
                    body = "Registre ${Stats.MIN_SESSIONS_FOR_CHARTS} treinos para ver " +
                        "volume, carga e recordes.",
                    ctaLabel = "Iniciar treino",
                    onCta = onStartWorkout,
                    modifier = Modifier.padding(top = 48.dp),
                )
            }
        }

        if (state.hasEnoughHistory) item {
            SerieSegmented(
                options = Stats.PERIOD_LABELS,
                selectedIndex = state.period,
                onSelect = viewModel::setPeriod,
                fillWidth = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (state.hasEnoughHistory && period != null) item {
            SerieCard(padding = Dimens.cardPaddingTight) {
                Text("Volume total", style = Nocturne.type.caption, color = c.textMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        period.volume,
                        style = Nocturne.type.display,
                        color = c.text,
                        modifier = Modifier.alignByBaseline(),
                    )
                    Text(
                        period.delta,
                        style = Nocturne.type.caption,
                        color = c.accent,
                        modifier = Modifier.alignByBaseline(),
                    )
                }
                VolumeBars(period.bars, Modifier.padding(top = 4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    period.axis.forEach {
                        Text(it, style = Nocturne.type.caption, color = c.textMuted)
                    }
                }
            }
        }

        if (state.hasEnoughHistory && progress != null) item {
            SerieCard(padding = Dimens.cardPaddingTight) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = viewModel::nextProgressExercise),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        progress.name,
                        style = Nocturne.type.itemTitle,
                        color = c.text,
                        modifier = Modifier.weight(1f),
                    )
                    PhIcon(Ph.caretDown, size = 16.dp, tint = c.textMuted)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1RM est.", style = Nocturne.type.caption, color = c.textMuted)
                    Text(progress.estimated1RM, style = Nocturne.type.itemTitle, color = c.accent)
                }
                ProgressLine(progress, Modifier.padding(vertical = 4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(progress.from, style = Nocturne.type.caption, color = c.textMuted)
                    Text(progress.to, style = Nocturne.type.caption, color = c.accentRamp.s300)
                }
            }
        }

        if (state.hasEnoughHistory) item {
            SerieCard(padding = Dimens.cardPaddingTight) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${state.totalWorkouts}", style = Nocturne.type.h1, color = c.text)
                        Text(
                            Stats.workoutsPerWeek(state.history, state.today),
                            style = Nocturne.type.caption,
                            color = c.textMuted,
                        )
                    }
                    VerticalHairline()
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${state.totalRecords}", style = Nocturne.type.h1, color = c.text)
                        Text(
                            "recordes pessoais",
                            style = Nocturne.type.caption,
                            color = c.textMuted,
                        )
                    }
                }
            }
        }

        if (state.hasEnoughHistory) item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    SectionLabel("Volume por grupo", modifier = Modifier.weight(1f))
                    Stats.neglectedGroup(state.muscleVolumes)?.let {
                        SerieTag(it, style = SerieTagStyle.Accent2)
                    }
                }
                state.muscleVolumes.forEachIndexed { i, muscle ->
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            muscle.name,
                            style = Nocturne.type.bodySmall,
                            color = c.textSecondary,
                            modifier = Modifier.width(72.dp),
                        )
                        Box(
                            Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(c.neutral.s800)
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth(muscle.weight / 100f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(3.dp))
                                    // O ultimo grupo e o que esta abaixo da meta:
                                    // recebe o acento para puxar a atencao.
                                    .background(
                                        if (i == state.muscleVolumes.lastIndex) c.accent
                                        else c.neutral.s400
                                    )
                            )
                        }
                        Text(
                            muscle.percent,
                            style = Nocturne.type.caption,
                            color = c.textMuted,
                            modifier = Modifier.width(32.dp),
                        )
                    }
                }
            }
        }

        // Fica fora do condicional dos graficos de proposito: com uma ou duas
        // sessoes os graficos nao aparecem, e o historico precisa continuar
        // alcancavel - e onde se apaga um registro errado.
        if (state.history.isNotEmpty()) item {
            ListRow(
                onClick = onOpenHistory,
                leading = { PhIcon(Ph.stack, size = 20.dp, tint = c.textSecondary) },
                trailing = { PhIcon(Ph.caretRight, size = 16.dp, tint = c.textMuted) },
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Column {
                    Text("Histórico", style = Nocturne.type.itemTitle, color = c.text)
                    Text(
                        "${state.history.size} treinos registrados",
                        style = Nocturne.type.caption,
                        color = c.textMuted,
                    )
                }
            }
        }
    }
}

/** Barras de volume. A ultima recebe o acento e brilho por ser o periodo atual. */
@Composable
private fun VolumeBars(bars: List<Int>, modifier: Modifier = Modifier) {
    val c = Nocturne.colors
    Row(
        modifier = modifier.fillMaxWidth().height(96.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        bars.forEachIndexed { i, value ->
            val last = i == bars.lastIndex
            Box(
                Modifier
                    .weight(1f)
                    // Barras zeradas viram um tracinho de 4%: some de vez
                    // seria lido como dado faltando, nao como dia de descanso.
                    .fillMaxHeight(maxOf(value, 4) / 100f)
                    .then(
                        if (last) Modifier.accentGlow(RoundedCornerShape(3.dp, 3.dp, 0.dp, 0.dp), 0.5f)
                        else Modifier
                    )
                    .clip(RoundedCornerShape(3.dp, 3.dp, 0.dp, 0.dp))
                    .background(if (last) c.accent else c.neutral.s700)
            )
        }
    }
}

/** Linha de progressao de carga, com um ponto marcando o valor mais recente. */
@Composable
private fun ProgressLine(progress: ExerciseProgress, modifier: Modifier = Modifier) {
    val c = Nocturne.colors
    Canvas(modifier.fillMaxWidth().height(72.dp)) {
        val points = progress.points
        if (points.size < 2) return@Canvas
        val stepX = size.width / (points.size - 1)
        fun yFor(v: Float) = size.height - (v / 100f) * size.height

        val path = Path().apply {
            moveTo(0f, yFor(points.first()))
            points.forEachIndexed { i, v -> if (i > 0) lineTo(i * stepX, yFor(v)) }
        }
        drawPath(
            path = path,
            color = c.accent,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )
        drawCircle(
            color = c.accent,
            radius = 4.dp.toPx(),
            center = Offset(size.width, yFor(points.last())),
        )
    }
}
