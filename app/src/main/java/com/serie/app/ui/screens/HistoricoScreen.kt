package com.serie.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.serie.app.domain.CompletedSession
import com.serie.app.domain.DateFormat
import com.serie.app.domain.agree
import com.serie.app.domain.plural
import com.serie.app.domain.Stats
import com.serie.app.ui.SerieUiState
import com.serie.app.ui.SerieViewModel
import com.serie.app.ui.components.EmptyState
import com.serie.app.ui.components.ListRow
import com.serie.app.ui.components.Ph
import com.serie.app.ui.components.PhIcon
import com.serie.app.ui.components.ScreenHeader
import com.serie.app.ui.components.SerieButton
import com.serie.app.ui.components.SerieButtonStyle
import com.serie.app.ui.components.WorkoutBadge
import com.serie.app.ui.formatClock
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.Radius
import com.serie.app.ui.theme.elevLg
import com.serie.app.ui.theme.elevSm

/** Histórico de treinos concluídos, com exclusão. */
@Composable
fun HistoricoScreen(
    state: SerieUiState,
    viewModel: SerieViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Nocturne.colors

    LazyColumn(
        modifier = modifier.fillMaxSize().background(c.bg),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = Dimens.screenMargin,
            end = Dimens.screenMargin,
            bottom = Dimens.blockGapLarge,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            ScreenHeader(title = "Histórico", navIcon = Ph.arrowLeft, onNav = onBack)
        }

        if (state.history.isEmpty()) {
            item {
                EmptyState(
                    icon = Ph.barbell,
                    title = "Nenhum treino registrado",
                    body = "Os treinos que você concluir aparecem aqui.",
                    ctaLabel = "Voltar",
                    onCta = onBack,
                    modifier = Modifier.padding(top = 48.dp),
                )
            }
        } else {
            item {
                Text(
                    plural(state.history.size, "treino") + " · " +
                        Stats.formatVolume(state.history.sumOf { it.volumeKg }) +
                        " no total",
                    style = Nocturne.type.caption,
                    color = c.textMuted,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            items(state.history, key = { it.id }) { session ->
                SessionRow(session, onDelete = { viewModel.askDeleteSession(session) })
            }
        }
    }

    state.sessionToDelete?.let { session ->
        ConfirmDeleteDialog(
            session = session,
            onDismiss = viewModel::dismissDeleteSession,
            onConfirm = viewModel::confirmDeleteSession,
        )
    }
}

@Composable
private fun SessionRow(session: CompletedSession, onDelete: () -> Unit) {
    val c = Nocturne.colors
    ListRow(
        leading = { WorkoutBadge(session.workoutLetter, highlighted = false) },
        trailing = {
            PhIcon(
                Ph.trash,
                size = 18.dp,
                tint = c.textMuted,
                modifier = Modifier
                    .clip(Radius.md)
                    .clickable(onClick = onDelete)
                    .padding(6.dp),
            )
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(session.workoutName, style = Nocturne.type.itemTitle, color = c.text)
            Text(
                DateFormat.shortDate(session.date) + " · " +
                    DateFormat.timeRange(session.startedAtMillis, session.finishedAtMillis),
                style = Nocturne.type.caption,
                color = c.textMuted,
            )
            Text(
                plural(session.totalSets, "série") + " · " +
                    Stats.formatVolume(session.volumeKg) + " · " +
                    formatClock(session.durationSeconds),
                style = Nocturne.type.caption,
                color = c.textSecondary,
            )
        }
    }
}

/**
 * Confirmacao antes de apagar.
 *
 * Apagar uma sessao mexe em sequencia, volume e recordes de uma vez, e nao ha
 * como desfazer - entao a acao pede confirmacao em vez de acontecer no toque.
 */
@Composable
private fun ConfirmDeleteDialog(
    session: CompletedSession,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
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
            Text("Apagar este treino?", style = Nocturne.type.h2, color = c.text)
            Text(
                "${session.workoutName} · ${DateFormat.shortDate(session.date)}\n" +
                    plural(session.totalSets, "série") + " " +
                    agree(session.totalSets, "registrada") +
                    (if (session.totalSets == 1) " some" else " somem") +
                    ", e sequência, volume e recordes serão recalculados.",
                style = Nocturne.type.bodySmall,
                color = c.textSecondary,
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SerieButton(
                    label = "Cancelar",
                    onClick = onDismiss,
                    style = SerieButtonStyle.Secondary,
                    modifier = Modifier.weight(1f),
                )
                SerieButton(
                    label = "Apagar",
                    onClick = onConfirm,
                    leadingIcon = Ph.trash,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
