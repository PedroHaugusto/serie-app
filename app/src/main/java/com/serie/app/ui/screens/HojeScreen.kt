package com.serie.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.serie.app.data.SeedData
import com.serie.app.domain.DayState
import com.serie.app.domain.WeekDay
import com.serie.app.ui.SerieUiState
import com.serie.app.ui.SerieViewModel
import com.serie.app.ui.components.EmptyState
import com.serie.app.ui.components.Ph
import com.serie.app.ui.components.PhIcon
import com.serie.app.ui.components.SectionLabel
import com.serie.app.ui.components.SerieBlockButton
import com.serie.app.ui.components.SerieCard
import com.serie.app.ui.components.SerieTag
import com.serie.app.ui.components.StatTile
import com.serie.app.ui.components.VerticalHairline
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.Radius
import com.serie.app.ui.theme.elevSm

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
        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(SeedData.TODAY_LABEL, style = Nocturne.type.label, color = c.textMuted)
                    // Primeiro nome do perfil: com o nome editavel, uma
                    // saudacao fixa passaria a chamar o usuario de outra pessoa.
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
                    Text(
                        state.profile.initial,
                        style = Nocturne.type.itemTitle,
                        color = c.accent,
                    )
                }
            }
        }

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
            item {
                SerieCard(halo = true) {
                    Text("TREINO DO DIA", style = Nocturne.type.kicker, color = c.accentRamp.s300)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            today.letter,
                            style = Nocturne.type.display,
                            color = c.accent,
                            modifier = Modifier.alignByBaseline(),
                        )
                        Text(
                            today.name,
                            style = Nocturne.type.h2,
                            color = c.text,
                            modifier = Modifier.alignByBaseline(),
                        )
                    }
                    Text(
                        "${today.exercises.size} exercícios · ${today.totalSets} séries · " +
                            "~${today.estimatedMinutes} min",
                        style = Nocturne.type.bodySmall,
                        color = c.textSecondary,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        today.muscleTags.forEach { SerieTag(it) }
                    }
                    // Com sessao guardada o rotulo muda: tocar aqui retoma o
                    // que ja estava registrado em vez de comecar de novo.
                    SerieBlockButton(
                        label = if (state.active != null) "Continuar treino" else "Iniciar treino",
                        onClick = { onStartWorkout(today.id) },
                        leadingIcon = Ph.play,
                        leadingIconFilled = true,
                        trailingIcon = Ph.arrowRight,
                        spread = true,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionLabel("Esta semana", trailing = SeedData.WEEK_PROGRESS)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    SeedData.week.forEach { day ->
                        WeekDayCell(day, Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            SerieCard(padding = Dimens.cardPaddingTight) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    StatTile(
                        icon = Ph.flame,
                        value = SeedData.STREAK_DAYS,
                        caption = "dias seguidos",
                        filled = true,
                        modifier = Modifier.weight(1f),
                    )
                    VerticalHairline()
                    StatTile(
                        icon = Ph.stack,
                        value = SeedData.WEEK_VOLUME,
                        caption = "volume semana",
                        modifier = Modifier.weight(1f),
                    )
                    VerticalHairline()
                    StatTile(
                        icon = Ph.trendUp,
                        value = SeedData.MONTH_DELTA,
                        caption = "vs. mês passado",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

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
                    Text(
                        SeedData.lastRecord.exerciseName,
                        style = Nocturne.type.itemTitle,
                        color = c.text,
                    )
                    Text(
                        SeedData.lastRecord.detail,
                        style = Nocturne.type.caption,
                        color = c.textSecondary,
                    )
                }
                PhIcon(Ph.caretRight, size = 16.dp, tint = c.textMuted)
            }
        }
    }
}

/** Um dia na faixa da semana: circulo de 34 dp com o estado do dia. */
@Composable
private fun WeekDayCell(day: WeekDay, modifier: Modifier = Modifier) {
    val c = Nocturne.colors
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(day.label, style = Nocturne.type.caption, color = c.textMuted)
        Box(
            Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    when (day.state) {
                        DayState.Done -> c.accentRamp.s900
                        DayState.Today -> c.accent
                        else -> c.surface
                    }
                )
                .then(if (day.state == DayState.Planned) Modifier.elevSm(RoundedCornerShape(50)) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            when (day.state) {
                DayState.Done -> PhIcon(Ph.check, size = 14.dp, tint = c.accent)
                DayState.Today -> Text(
                    day.plannedLetter,
                    style = Nocturne.type.caption,
                    color = c.accentRamp.s100,
                )
                DayState.Planned -> Text(
                    day.plannedLetter,
                    style = Nocturne.type.caption,
                    color = c.textSecondary,
                )
                DayState.Rest -> PhIcon(Ph.moon, size = 12.dp, tint = c.neutral.s500)
            }
        }
    }
}
