package com.serie.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.serie.app.domain.DayState
import com.serie.app.domain.Stats
import com.serie.app.domain.WeekDay
import com.serie.app.domain.Workout
import com.serie.app.ui.SerieUiState
import com.serie.app.ui.SerieViewModel
import com.serie.app.ui.components.EmptyState
import com.serie.app.ui.components.Ph
import com.serie.app.ui.components.PhIcon
import com.serie.app.ui.components.SectionLabel
import com.serie.app.ui.components.SerieBlockButton
import com.serie.app.ui.components.SerieButtonStyle
import com.serie.app.ui.components.SerieCard
import com.serie.app.ui.components.SerieDivider
import com.serie.app.ui.components.SerieField
import com.serie.app.ui.components.SerieTag
import com.serie.app.ui.components.SerieTagStyle
import com.serie.app.ui.components.StatTile
import com.serie.app.ui.components.VerticalHairline
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.Radius
import com.serie.app.ui.theme.accentGlow
import com.serie.app.ui.theme.elevLg
import com.serie.app.ui.theme.elevSm

/** Quantos exercicios o cartao lista antes de resumir o resto. */
private const val PREVIEW_COUNT = 3

/**
 * "1 exercício" / "6 exercícios".
 *
 * Todas as palavras contadas nesta tela terminam em vogal, entao o plural e
 * sempre um "s" no fim - nao vale a pena uma tabela para isso.
 */
private fun plural(count: Int, singular: String): String =
    "$count $singular" + if (count == 1) "" else "s"

/** 02 · Hoje - a tela que abre o app depois do login. */
@Composable
fun HojeScreen(
    state: SerieUiState,
    viewModel: SerieViewModel,
    onStartWorkout: (String) -> Unit,
    onOpenRecords: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Nocturne.colors
    val today = viewModel.todayWorkout(state)

    LazyColumn(
        modifier = modifier.fillMaxSize().background(c.bg),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = Dimens.screenMargin,
            end = Dimens.screenMargin,
            top = 8.dp,
            bottom = Dimens.blockGapLarge,
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.blockGapLarge),
    ) {
        item { Header(state) }

        if (today == null) {
            item {
                EmptyState(
                    icon = Ph.moonStars,
                    title = "Dia de descanso",
                    body = "Nenhum treino agendado para hoje. Quer adiantar o próximo?",
                    ctaLabel = "Escolher treino",
                    onCta = {},
                    modifier = Modifier.padding(top = 40.dp),
                )
            }
        } else {
            item { TodayCard(state, today, onStartWorkout) }
        }

        item { WeekSection(state) }

        item { MetricsRow(state, viewModel) }

        // Sem recorde no historico, o card simplesmente nao existe: um card
        // vazio sugeriria que o usuario tem um recorde e nao bateu.
        state.lastRecord?.let { record ->
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Radius.lg)
                        .background(c.accentRamp.s900)
                        .clickable(onClick = onOpenRecords)
                        .padding(Dimens.cardPaddingTight),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(Radius.md)
                            .background(c.accentRamp.s800),
                        contentAlignment = Alignment.Center,
                    ) { PhIcon(Ph.trophy, size = 18.dp, tint = c.accent, filled = true) }
                    Column(Modifier.weight(1f)) {
                        Text(record.exerciseName, style = Nocturne.type.itemTitle, color = c.text)
                        Text(
                            record.detail,
                            style = Nocturne.type.caption,
                            color = c.textSecondary,
                        )
                    }
                    PhIcon(Ph.caretRight, size = 16.dp, tint = c.textMuted)
                }
            }
        }
    }
}

@Composable
private fun Header(state: SerieUiState) {
    val c = Nocturne.colors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(state.todayLabel, style = Nocturne.type.label, color = c.textMuted)
            // Primeiro nome do perfil: com o nome editavel, uma saudacao fixa
            // passaria a chamar o usuario de outra pessoa.
            Text(
                "Bom dia, ${state.profile.name.trim().substringBefore(' ')}",
                style = Nocturne.type.h1,
                color = c.text,
            )
        }
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(50))
                .background(c.accentRamp.s900),
            contentAlignment = Alignment.Center,
        ) {
            Text(state.profile.initial, style = Nocturne.type.itemTitle, color = c.accent)
        }
    }
}

/**
 * O cartao do treino do dia.
 *
 * Mostra o que vem pela frente - os exercicios, nao so a contagem deles - e em
 * que ponto o treino esta: nao comecado, em andamento com progresso, ou ja
 * concluido hoje. Antes o cartao parecia igual nos tres casos.
 */
@Composable
private fun TodayCard(
    state: SerieUiState,
    workout: Workout,
    onStartWorkout: (String) -> Unit,
) {
    val c = Nocturne.colors
    val active = state.active?.takeIf { it.workoutId == workout.id }
    val doneToday = state.todaySessions.filter { it.workoutId == workout.id }

    SerieCard(halo = true) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("TREINO DO DIA", style = Nocturne.type.kicker, color = c.accentRamp.s300)
            when {
                active != null -> SerieTag("Em andamento", style = SerieTagStyle.Accent)
                doneToday.isNotEmpty() -> SerieTag(
                    "Concluído hoje",
                    style = SerieTagStyle.Accent,
                    icon = Ph.check,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                workout.letter,
                style = Nocturne.type.display,
                color = c.accent,
                modifier = Modifier.alignByBaseline(),
            )
            Text(
                workout.name,
                style = Nocturne.type.h2,
                color = c.text,
                modifier = Modifier.alignByBaseline(),
            )
        }

        Text(
            plural(workout.exercises.size, "exercício") + " · " +
                plural(workout.totalSets, "série") + " · ~${workout.estimatedMinutes} min",
            style = Nocturne.type.bodySmall,
            color = c.textSecondary,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            workout.muscleTags.forEach { SerieTag(it) }
        }

        if (workout.exercises.isNotEmpty()) {
            SerieDivider(fade = 16.dp, modifier = Modifier.padding(vertical = 2.dp))
            // A prescricao de cada exercicio: da para decidir se o treino cabe
            // agora sem precisar inicia-lo para descobrir o que tem dentro.
            workout.exercises.take(PREVIEW_COUNT).forEachIndexed { index, exercise ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${index + 1}",
                        style = Nocturne.type.caption,
                        color = c.neutral.s500,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        exercise.name,
                        style = Nocturne.type.bodySmall,
                        color = c.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "${exercise.sets} × ${exercise.reps}",
                        style = Nocturne.type.caption,
                        color = c.textMuted,
                    )
                }
            }
            val rest = workout.exercises.size - PREVIEW_COUNT
            if (rest > 0) {
                Text(
                    "+ " + plural(rest, "exercício"),
                    style = Nocturne.type.caption,
                    color = c.textMuted,
                )
            }
        }

        if (active != null) {
            SessionProgress(
                exerciseIndex = active.exerciseIndex,
                exerciseCount = active.exerciseCount,
                doneSets = active.sets.count { it.done },
                totalSets = workout.totalSets,
            )
        } else if (doneToday.isNotEmpty()) {
            val volume = Stats.formatVolume(doneToday.sumOf { it.volumeKg })
            val sets = doneToday.sumOf { it.totalSets }
            Text(
                plural(sets, "série") + " · $volume registrados hoje",
                style = Nocturne.type.caption,
                color = c.accentRamp.s300,
            )
        }

        SerieBlockButton(
            label = when {
                active != null -> "Continuar treino"
                doneToday.isNotEmpty() -> "Treinar de novo"
                else -> "Iniciar treino"
            },
            onClick = { onStartWorkout(workout.id) },
            leadingIcon = Ph.play,
            leadingIconFilled = true,
            trailingIcon = Ph.arrowRight,
            spread = true,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

/** Quanto do treino em andamento ja foi registrado. */
@Composable
private fun SessionProgress(
    exerciseIndex: Int,
    exerciseCount: Int,
    doneSets: Int,
    totalSets: Int,
) {
    val c = Nocturne.colors
    val fraction = if (totalSets <= 0) 0f else (doneSets.toFloat() / totalSets).coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "Exercício ${exerciseIndex + 1} de $exerciseCount",
                style = Nocturne.type.caption,
                color = c.accentRamp.s300,
            )
            Text(
                "$doneSets de " + plural(totalSets, "série"),
                style = Nocturne.type.caption,
                color = c.textMuted,
            )
        }
        ProgressTrack(fraction)
    }
}

/** Trilho de progresso de 6 dp, usado pelo treino em andamento e pela meta. */
@Composable
private fun ProgressTrack(fraction: Float, modifier: Modifier = Modifier) {
    val c = Nocturne.colors
    Box(
        modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(c.neutral.s800)
    ) {
        if (fraction > 0f) {
            Box(
                Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .accentGlow(RoundedCornerShape(3.dp), alpha = 0.45f)
                    .clip(RoundedCornerShape(3.dp))
                    .background(c.accent)
            )
        }
    }
}

/**
 * A semana.
 *
 * Cada dia treinado mostra a letra do treino que foi feito, nao um certo
 * generico: e isso que deixa ler de relance se a semana saiu como planejada.
 * Hoje ganha um anel proprio, que sobrevive a ja ter treinado.
 */
@Composable
private fun WeekSection(state: SerieUiState) {
    // Dias treinados sobre dias agendados, nao sessoes sobre a meta do
    // perfil: a meta e uma intencao solta, e contar sessoes fazia quatro
    // treinos na mesma quinta virarem "4 de 5" com um unico dia marcado.
    val trained = Stats.trainedDaysThisWeek(state.week)
    val scheduled = Stats.scheduledDaysThisWeek(state.workouts)
    val fraction = if (scheduled <= 0) 0f else (trained.toFloat() / scheduled).coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel(
            "Esta semana",
            trailing = if (scheduled > 0) {
                "$trained de " + plural(scheduled, "dia")
            } else {
                plural(trained, "dia") + " treinado" + if (trained == 1) "" else "s"
            },
        )
        ProgressTrack(fraction)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            state.week.forEach { day ->
                WeekDayCell(day, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun WeekDayCell(day: WeekDay, modifier: Modifier = Modifier) {
    val c = Nocturne.colors
    val shape = RoundedCornerShape(50)
    val done = day.state == DayState.Done

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            day.label,
            style = Nocturne.type.caption,
            color = if (day.isToday) c.accent else c.textMuted,
        )
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .then(if (done) Modifier.accentGlow(shape, alpha = 0.35f) else Modifier)
                .clip(shape)
                .background(
                    when (day.state) {
                        DayState.Done -> c.accent
                        DayState.Planned -> c.surface
                        DayState.Today -> c.accentRamp.s900
                        DayState.Rest -> c.surface
                    }
                )
                // Hoje mantem o anel mesmo depois de treinado: e a referencia
                // de onde a semana esta.
                .then(
                    if (day.isToday) {
                        Modifier.border(2.dp, c.accent, shape)
                    } else {
                        Modifier.elevSm(shape)
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            when {
                done -> Text(
                    day.letter.ifEmpty { "✓" },
                    style = Nocturne.type.itemTitle,
                    color = c.accentRamp.s100,
                )

                day.state == DayState.Rest -> PhIcon(Ph.moon, size = 13.dp, tint = c.neutral.s500)

                else -> Text(
                    day.letter,
                    style = Nocturne.type.itemTitle,
                    color = if (day.isToday) c.accent else c.textSecondary,
                )
            }
        }
    }
}

/**
 * Peso corporal e variacao de volume.
 *
 * A sequencia de dias saiu: ela premia constancia, nao progresso, e some
 * inteira depois de um dia de folga. O peso ocupa o lugar porque e o numero
 * que o usuario acompanha de fato - e, diferente dos outros, ele so existe se
 * for registrado, entao a casa convida a isso quando esta vazia.
 */
@Composable
private fun MetricsRow(state: SerieUiState, viewModel: SerieViewModel) {
    val c = Nocturne.colors
    val latest = state.bodyWeights.maxByOrNull { it.atMillis }
    val change = Stats.weightChange(state.bodyWeights, state.today)

    SerieCard(padding = Dimens.cardPaddingTight) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(Radius.md)
                    .clickable(onClick = viewModel::openRecordWeight)
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                PhIcon(Ph.scales, size = 18.dp, tint = c.accent)
                Text(
                    latest?.let { Stats.formatWeight(it.kg) + " kg" } ?: "—",
                    style = Nocturne.type.h2,
                    color = c.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = when {
                        latest == null -> "registrar peso"
                        change == null -> "primeira pesagem"
                        else -> {
                            val (delta, days) = change
                            val sign = if (delta > 0) "+" else "−"
                            "$sign${Stats.formatWeight(kotlin.math.abs(delta))} kg · ${days}d"
                        }
                    },
                    style = Nocturne.type.caption,
                    color = if (latest == null) c.accent else c.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            VerticalHairline()
            StatTile(
                icon = Ph.trendUp,
                value = state.monthDelta,
                caption = "volume vs. mês passado",
                modifier = Modifier.weight(1f),
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
}
