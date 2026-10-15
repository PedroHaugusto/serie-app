package com.serie.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.serie.app.domain.SetEntry
import com.serie.app.ui.SerieUiState
import com.serie.app.ui.SerieViewModel
import com.serie.app.ui.components.Ph
import com.serie.app.ui.components.PhIcon
import com.serie.app.ui.components.SerieBlockButton
import com.serie.app.ui.components.SerieButtonStyle
import com.serie.app.ui.components.SerieCard
import com.serie.app.ui.components.SerieDivider
import com.serie.app.ui.components.SerieField
import com.serie.app.ui.formatClock
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.Radius
import com.serie.app.ui.theme.accentGlow
import com.serie.app.ui.theme.elevLg
import com.serie.app.ui.theme.elevSm

/** 06 · Treino em andamento - onde o app e de fato usado na academia. */
@Composable
fun ExecucaoScreen(
    state: SerieUiState,
    viewModel: SerieViewModel,
    onFinish: () -> Unit,
    onDiscarded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Nocturne.colors
    val active = state.active ?: return
    val hasPrevious = active.exerciseIndex > 0
    val hasNext = active.exerciseIndex < active.exerciseCount - 1
    val notes = state.library.firstOrNull { it.id == active.exerciseId }?.notes.orEmpty()

    Column(modifier.fillMaxSize().background(c.bg)) {
        TopBar(
            workoutName = active.workoutName,
            elapsedSeconds = active.elapsedSeconds,
            exerciseIndex = active.exerciseIndex,
            exerciseCount = active.exerciseCount,
            onDiscard = viewModel::askDiscardWorkout,
            onFinish = onFinish,
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = Dimens.screenMargin,
                end = Dimens.screenMargin,
                top = Dimens.blockGap,
                bottom = Dimens.blockGapLarge,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.blockGap),
        ) {
            item { ExerciseCard(active.exerciseName, active.goal, notes) }

            item { SetHeaderRow() }

            itemsIndexed(active.sets) { index, set ->
                SetRow(
                    index = index,
                    set = set,
                    isCurrent = index == active.currentSetIndex,
                    onLoadChange = { viewModel.updateSetLoad(index, it) },
                    onRepsChange = { viewModel.updateSetReps(index, it) },
                    onToggleDone = { viewModel.toggleSetDone(index) },
                )
            }

            item {
                SerieBlockButton(
                    label = "Série",
                    onClick = viewModel::addSet,
                    style = SerieButtonStyle.Secondary,
                    leadingIcon = Ph.plus,
                    height = Dimens.touchTargetMin,
                )
            }

            item {
                Row(
                    Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ExerciseStep(Ph.arrowLeft, hasPrevious, viewModel::previousExercise)
                    Text(
                        "Exercício ${active.exerciseIndex + 1} de ${active.exerciseCount}",
                        style = Nocturne.type.segment,
                        color = c.textSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                    ExerciseStep(Ph.arrowRight, hasNext, viewModel::nextExercise)
                }
            }
        }

        RestBar(
            seconds = active.restSeconds,
            totalSeconds = active.restTotalSeconds,
            running = active.restRunning,
            nextSet = active.currentSetIndex.let { if (it == -1) null else it + 1 },
            onMinus = { viewModel.adjustRest(-15) },
            onPlus = { viewModel.adjustRest(15) },
        )
    }

    if (state.discardingWorkout) {
        DiscardDialog(
            onDismiss = viewModel::dismissDiscardWorkout,
            onConfirm = {
                viewModel.confirmDiscardWorkout()
                onDiscarded()
            },
        )
    }
}

/**
 * Cabecalho.
 *
 * O cronometro ganhou rotulo: solto ao lado do nome do treino, nao dava para
 * saber se contava o treino, o exercicio ou o descanso.
 */
@Composable
private fun TopBar(
    workoutName: String,
    elapsedSeconds: Int,
    exerciseIndex: Int,
    exerciseCount: Int,
    onDiscard: () -> Unit,
    onFinish: () -> Unit,
) {
    val c = Nocturne.colors
    Column(
        Modifier
            .fillMaxWidth()
            .background(c.surface)
            .padding(horizontal = Dimens.screenMargin, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onDiscard),
                contentAlignment = Alignment.Center,
            ) { PhIcon(Ph.x, size = 20.dp, tint = c.textMuted) }

            Text(
                workoutName,
                style = Nocturne.type.itemTitle,
                color = c.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            Text(
                "Finalizar",
                style = Nocturne.type.button,
                color = c.accent,
                modifier = Modifier
                    .clip(Radius.md)
                    .clickable(onClick = onFinish)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            )
        }

        Row(
            Modifier.fillMaxWidth().padding(start = 44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                "Exercício ${exerciseIndex + 1} de $exerciseCount",
                style = Nocturne.type.caption,
                color = c.textMuted,
                modifier = Modifier.weight(1f),
            )
            PhIcon(Ph.timer, size = 13.dp, tint = c.textMuted)
            Text(
                "${formatClock(elapsedSeconds)} de treino",
                style = Nocturne.type.caption,
                color = c.textMuted,
            )
        }
    }
}

/** Nome, meta e dica num bloco so, em vez de tres pedacos soltos. */
@Composable
private fun ExerciseCard(name: String, goal: String, notes: String) {
    val c = Nocturne.colors
    SerieCard(padding = Dimens.cardPaddingTight, gap = 6.dp) {
        Text(name, style = Nocturne.type.h1, color = c.text)
        if (goal.isNotBlank()) {
            Text(goal, style = Nocturne.type.bodySmall, color = c.textSecondary)
        }
        if (notes.isNotBlank()) {
            SerieDivider(fade = 16.dp, modifier = Modifier.padding(vertical = 4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PhIcon(Ph.info, size = 15.dp, tint = c.accent)
                Text(notes, style = Nocturne.type.bodySmall, color = c.textSecondary)
            }
        }
    }
}

@Composable
private fun SetHeaderRow() {
    val c = Nocturne.colors
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Série", style = Nocturne.type.caption, color = c.textMuted, modifier = Modifier.width(44.dp))
        Text("Anterior", style = Nocturne.type.caption, color = c.textMuted, modifier = Modifier.weight(1f))
        Text("kg", style = Nocturne.type.caption, color = c.textMuted, modifier = Modifier.width(64.dp), textAlign = TextAlign.Center)
        Text("Reps", style = Nocturne.type.caption, color = c.textMuted, modifier = Modifier.width(56.dp), textAlign = TextAlign.Center)
        Box(Modifier.size(36.dp))
    }
}

/**
 * Uma serie. A serie atual ganha contorno de acento para o usuario achar onde
 * esta sem ler; as ja fechadas esmaecem, e as futuras ficam neutras.
 */
@Composable
private fun SetRow(
    index: Int,
    set: SetEntry,
    isCurrent: Boolean,
    onLoadChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onToggleDone: () -> Unit,
) {
    val c = Nocturne.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Radius.md)
            .background(if (set.done) c.accentRamp.s900 else c.surface)
            .then(
                if (isCurrent) Modifier.border(Dimens.hairline, c.accent, Radius.md)
                else Modifier.elevSm(Radius.md)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            "${index + 1}",
            style = Nocturne.type.itemTitle,
            color = if (set.done) c.accent else c.text,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.Center,
        )
        Text(
            set.previous,
            style = Nocturne.type.caption,
            color = c.textMuted,
            modifier = Modifier.weight(1f),
        )
        SerieField(
            value = set.load,
            onValueChange = onLoadChange,
            keyboardType = KeyboardType.Number,
            modifier = Modifier.width(64.dp),
        )
        SerieField(
            value = set.reps,
            onValueChange = onRepsChange,
            keyboardType = KeyboardType.Number,
            modifier = Modifier.width(56.dp),
        )
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(50))
                .background(if (set.done) c.accent else c.neutral.s800)
                .clickable(onClick = onToggleDone),
            contentAlignment = Alignment.Center,
        ) {
            PhIcon(
                Ph.check,
                size = 16.dp,
                tint = if (set.done) c.accentRamp.s100 else c.neutral.s500,
            )
        }
    }
}

/**
 * Barra de descanso.
 *
 * Tem dois estados deliberadamente diferentes. Parada, ela diz que so comeca
 * quando uma serie for marcada - antes exibia um numero imovel que parecia um
 * cronometro travado. Correndo, ganha acento, brilho e a faixa de progresso,
 * para ser lida de longe no meio do treino.
 */
@Composable
private fun RestBar(
    seconds: Int,
    totalSeconds: Int,
    running: Boolean,
    nextSet: Int?,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
) {
    val c = Nocturne.colors

    Column(Modifier.fillMaxWidth().background(c.surface)) {
        Box(Modifier.fillMaxWidth().height(3.dp).background(c.divider)) {
            if (running) {
                // Preenche conforme o descanso passa, medido contra a duracao
                // deste exercicio - nao contra um valor fixo.
                val elapsed = 1f - (seconds.toFloat() / totalSeconds.coerceAtLeast(1))
                Box(
                    Modifier
                        .fillMaxWidth(elapsed.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .accentGlow(RoundedCornerShape(2.dp), alpha = 0.6f)
                        .background(c.accent)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.screenMargin, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PhIcon(
                Ph.timer,
                size = 20.dp,
                tint = if (running) c.accent else c.neutral.s500,
                filled = running,
            )
            Column(Modifier.weight(1f)) {
                Text(
                    if (running) "Descansando" else "Descanso",
                    style = Nocturne.type.itemTitle,
                    color = if (running) c.accent else c.textSecondary,
                )
                Text(
                    when {
                        running && nextSet != null -> "depois vem a série $nextSet"
                        running -> "última série fechada"
                        else -> "começa ao marcar uma série"
                    },
                    style = Nocturne.type.caption,
                    color = c.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                formatClock(seconds),
                style = Nocturne.type.h2,
                color = if (running) c.accent else c.neutral.s500,
            )
            RestStep("−15s", onMinus)
            RestStep("+15s", onPlus)
        }
    }
}

@Composable
private fun RestStep(label: String, onClick: () -> Unit) {
    val c = Nocturne.colors
    Text(
        label,
        style = Nocturne.type.segment,
        color = c.textSecondary,
        modifier = Modifier
            .clip(Radius.md)
            .background(c.neutral.s800)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
    )
}

/** Seta de avancar ou voltar exercicio. */
@Composable
private fun ExerciseStep(icon: PhIcon, enabled: Boolean, onClick: () -> Unit) {
    val c = Nocturne.colors
    Box(
        Modifier
            .size(Dimens.touchTargetMin)
            .clip(Radius.md)
            .background(c.neutral.s800)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        PhIcon(icon, size = 18.dp, tint = if (enabled) c.textSecondary else c.neutral.s600)
    }
}

/**
 * Confirmacao de descarte.
 *
 * Descartar joga fora o que foi registrado e nao entra no historico - o
 * oposto de Finalizar, que fica ao lado. A confirmacao existe para que os dois
 * nao sejam confundidos num toque apressado.
 */
@Composable
private fun DiscardDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val c = Nocturne.colors
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .elevLg(Radius.lg)
                .clip(Radius.lg)
                .background(c.surface)
                .elevSm(Radius.lg)
                .padding(Dimens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.blockGap),
        ) {
            Text("Descartar treino?", style = Nocturne.type.h2, color = c.text)
            Text(
                "O que você registrou será perdido e nada entra no histórico. " +
                    "Para guardar, use Finalizar.",
                style = Nocturne.type.bodySmall,
                color = c.textSecondary,
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SerieBlockButton(
                    label = "Continuar treino",
                    onClick = onDismiss,
                    style = SerieButtonStyle.Secondary,
                    modifier = Modifier.weight(1f),
                )
                SerieBlockButton(
                    label = "Descartar",
                    onClick = onConfirm,
                    leadingIcon = Ph.trash,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
