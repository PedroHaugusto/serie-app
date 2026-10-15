package com.serie.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.serie.app.data.SeedData
import com.serie.app.domain.Workout
import com.serie.app.ui.SerieUiState
import com.serie.app.ui.SerieViewModel
import com.serie.app.ui.components.EmptyState
import com.serie.app.ui.components.ListRow
import com.serie.app.ui.components.Ph
import com.serie.app.ui.components.PhIcon
import com.serie.app.ui.components.ScreenHeader
import com.serie.app.ui.components.SectionLabel
import com.serie.app.ui.components.SerieExtendedFab
import com.serie.app.ui.components.SerieSegmented
import com.serie.app.ui.components.SerieTag
import com.serie.app.ui.components.SerieTagStyle
import com.serie.app.ui.components.WorkoutBadge
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne

/** 03 · Treinos - a lista com filtro, criacao e acesso a edicao. */
@Composable
fun TreinosScreen(
    state: SerieUiState,
    viewModel: SerieViewModel,
    onEditWorkout: (String) -> Unit,
    onNewWorkout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Nocturne.colors
    val workouts = viewModel.visibleWorkouts(state)

    Box(modifier.fillMaxSize().background(c.bg)) {
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = Dimens.screenMargin,
                end = Dimens.screenMargin,
                bottom = 88.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.blockGap),
        ) {
            item {
                ScreenHeader(
                    title = "Treinos",
                    actionIcon = Ph.magnifyingGlass,
                    onAction = {},
                )
            }

            item {
                SerieSegmented(
                    options = listOf("Todos · ${state.workouts.size}", "Força", "Cardio"),
                    selectedIndex = state.workoutFilter,
                    onSelect = viewModel::setWorkoutFilter,
                    fillWidth = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (workouts.isEmpty()) {
                item {
                    EmptyState(
                        icon = Ph.barbell,
                        title = "Nenhum treino ainda",
                        body = "Crie o primeiro treino ou comece por um programa pronto.",
                        ctaLabel = "Criar treino",
                        onCta = onNewWorkout,
                        modifier = Modifier.padding(top = 40.dp),
                    )
                }
            } else {
                items(workouts, key = { it.id }) { workout ->
                    WorkoutRow(
                        workout = workout,
                        isToday = viewModel.isScheduledToday(state, workout),
                        onClick = { onEditWorkout(workout.id) },
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SectionLabel("Programas prontos")
                    SeedData.readyPrograms.forEach { (name, frequency) ->
                        ListRow(
                            onClick = onNewWorkout,
                            trailing = {
                                PhIcon(Ph.caretRight, size = 16.dp, tint = c.textMuted)
                            },
                        ) {
                            Column {
                                Text(name, style = Nocturne.type.itemTitle, color = c.text)
                                Text(
                                    frequency,
                                    style = Nocturne.type.caption,
                                    color = c.textMuted,
                                )
                            }
                        }
                    }
                }
            }
        }

        SerieExtendedFab(
            label = "Novo treino",
            icon = Ph.plus,
            onClick = onNewWorkout,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Dimens.screenMargin),
        )
    }
}

@Composable
private fun WorkoutRow(workout: Workout, isToday: Boolean, onClick: () -> Unit) {
    val c = Nocturne.colors
    ListRow(
        onClick = onClick,
        leading = { WorkoutBadge(workout.letter, highlighted = isToday) },
        trailing = { PhIcon(Ph.dotsThreeVertical, size = 18.dp, tint = c.textMuted) },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(workout.name, style = Nocturne.type.itemTitle, color = c.text)
                if (isToday) SerieTag("Hoje", style = SerieTagStyle.Accent)
            }
            Text(workout.subtitle(), style = Nocturne.type.caption, color = c.textMuted)
        }
    }
}
