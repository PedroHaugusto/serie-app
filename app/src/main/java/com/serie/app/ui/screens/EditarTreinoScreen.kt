package com.serie.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.serie.app.domain.WorkoutExercise
import com.serie.app.ui.SerieUiState
import com.serie.app.ui.SerieViewModel
import com.serie.app.ui.components.Ph
import com.serie.app.ui.components.PhIcon
import com.serie.app.ui.components.ScreenHeader
import com.serie.app.ui.components.SectionLabel
import com.serie.app.ui.components.SerieBlockButton
import com.serie.app.ui.components.SerieButtonStyle
import com.serie.app.ui.components.SerieField
import com.serie.app.ui.components.SerieTag
import com.serie.app.ui.components.SerieTagStyle
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.Radius
import com.serie.app.ui.theme.elevSm

private val WEEK_LABELS = listOf("S", "T", "Q", "Q", "S", "S", "D")

/** 04 · Criar / editar treino. */
@Composable
fun EditarTreinoScreen(
    state: SerieUiState,
    viewModel: SerieViewModel,
    onClose: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onAddExercise: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Nocturne.colors
    val draft = state.draft ?: return

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
                title = if (state.draftIsNew) "Novo treino" else "Editar treino",
                navIcon = Ph.x,
                onNav = onClose,
                actionLabel = "Salvar",
                onAction = onSave,
            )
        }

        item {
            SerieField(
                value = draft.name,
                onValueChange = viewModel::updateDraftName,
                label = "Nome do treino",
                placeholder = "Costas e bíceps",
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Dias da semana", style = Nocturne.type.label, color = c.text.copy(alpha = 0.7f))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    WEEK_LABELS.forEachIndexed { i, label ->
                        val on = draft.days[i]
                        Box(
                            Modifier
                                .weight(1f)
                                .size(40.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (on) c.accentRamp.s900 else c.surface)
                                .then(if (!on) Modifier.elevSm(RoundedCornerShape(50)) else Modifier)
                                .clickable { viewModel.toggleDraftDay(i) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                label,
                                style = Nocturne.type.itemTitle,
                                color = if (on) c.accent else c.textMuted,
                            )
                        }
                    }
                }
            }
        }

        item {
            SectionLabel(
                "Exercícios · ${draft.exercises.size}",
                trailing = "Toque para editar",
            )
        }

        itemsIndexed(draft.exercises, key = { _, e -> e.id }) { index, exercise ->
            ExerciseEditorRow(
                exercise = exercise,
                expanded = state.expandedExercise == index,
                canMoveUp = index > 0,
                canMoveDown = index < draft.exercises.lastIndex,
                onToggle = { viewModel.toggleExerciseExpanded(index) },
                onMoveUp = { viewModel.moveDraftExercise(index, index - 1) },
                onMoveDown = { viewModel.moveDraftExercise(index, index + 1) },
                onRemove = { viewModel.removeDraftExercise(index) },
                onChange = { transform -> viewModel.updateDraftExercise(index, transform) },
            )
        }

        item {
            SerieBlockButton(
                label = "Adicionar exercício",
                onClick = onAddExercise,
                style = SerieButtonStyle.Secondary,
                leadingIcon = Ph.plus,
                height = Dimens.touchTargetMin,
            )
        }

        if (!state.draftIsNew) {
            item {
                Row(
                    modifier = Modifier
                        .clip(Radius.md)
                        .clickable(onClick = onDelete)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    PhIcon(Ph.trash, size = 16.dp, tint = c.accent2)
                    Text("Excluir treino", style = Nocturne.type.button, color = c.accent2)
                }
            }
        }
    }
}

/**
 * Linha de exercicio no editor. Fechada mostra o resumo da prescricao; aberta
 * expoe series, reps, carga e descanso como campos editaveis.
 *
 * A reordenacao e por botoes de subir e descer em vez de arrastar: arrastar
 * dentro de uma lista que ja rola exige capturar o gesto, e os botoes dao o
 * mesmo resultado com alvo de toque previsivel.
 */
@Composable
private fun ExerciseEditorRow(
    exercise: WorkoutExercise,
    expanded: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onToggle: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    onChange: ((WorkoutExercise) -> WorkoutExercise) -> Unit,
) {
    val c = Nocturne.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Radius.lg)
            .background(c.surface)
            .elevSm(Radius.lg)
            .padding(Dimens.cardPaddingTight),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(exercise.name, style = Nocturne.type.itemTitle, color = c.text)
                Text(exercise.summary, style = Nocturne.type.caption, color = c.textMuted)
            }
            PhIcon(
                if (expanded) Ph.caretUp else Ph.caretDown,
                size = 16.dp,
                tint = c.textMuted,
            )
        }

        if (expanded) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SerieField(
                    value = exercise.sets.toString(),
                    onValueChange = { v ->
                        onChange { it.copy(sets = v.toIntOrNull()?.coerceIn(1, 20) ?: it.sets) }
                    },
                    label = "Séries",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                )
                SerieField(
                    value = exercise.reps,
                    onValueChange = { v -> onChange { it.copy(reps = v) } },
                    label = "Reps",
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SerieField(
                    value = exercise.load,
                    onValueChange = { v -> onChange { it.copy(load = v) } },
                    label = "Carga",
                    modifier = Modifier.weight(1f),
                )
                SerieField(
                    value = exercise.restSeconds.toString(),
                    onValueChange = { v ->
                        onChange {
                            it.copy(restSeconds = v.toIntOrNull()?.coerceIn(0, 600) ?: it.restSeconds)
                        }
                    },
                    label = "Descanso (s)",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                )
            }

            SerieTag(
                "Progressão: +2,5 kg ao fechar ${exercise.reps} reps",
                style = SerieTagStyle.Accent,
                icon = Ph.trendUp,
            )

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ReorderButton(Ph.caretUp, canMoveUp, onMoveUp)
                    ReorderButton(Ph.caretDown, canMoveDown, onMoveDown)
                }
                Text(
                    "Remover",
                    style = Nocturne.type.segment,
                    color = c.textSecondary,
                    modifier = Modifier
                        .clip(Radius.md)
                        .clickable(onClick = onRemove)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun ReorderButton(icon: PhIcon, enabled: Boolean, onClick: () -> Unit) {
    val c = Nocturne.colors
    Box(
        Modifier
            .size(36.dp)
            .clip(Radius.md)
            .background(c.neutral.s800)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        PhIcon(
            icon,
            size = 14.dp,
            tint = if (enabled) c.textSecondary else c.neutral.s600,
        )
    }
}
