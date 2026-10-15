package com.serie.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.serie.app.domain.LibraryExercise
import com.serie.app.domain.MuscleGroup
import com.serie.app.ui.SerieUiState
import com.serie.app.ui.SerieViewModel
import com.serie.app.ui.components.EmptyState
import com.serie.app.ui.components.ListRow
import com.serie.app.ui.components.Ph
import com.serie.app.ui.components.PhIcon
import com.serie.app.ui.components.ScreenHeader
import com.serie.app.ui.components.SerieField
import com.serie.app.ui.components.SerieSegmented
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.Radius

/** 05 · Biblioteca de exercicios - busca, filtro por grupo e selecao multipla. */
@Composable
fun BibliotecaScreen(
    state: SerieUiState,
    viewModel: SerieViewModel,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Nocturne.colors
    val results = viewModel.visibleLibrary(state)
    val pickedCount = state.libraryPicked.size

    Column(modifier.fillMaxSize().background(c.bg)) {
        ScreenHeader(
            title = "Biblioteca",
            navIcon = Ph.arrowLeft,
            onNav = onBack,
            actionLabel = if (pickedCount > 0) "Adicionar ($pickedCount)" else "Adicionar",
            onAction = onConfirm,
            modifier = Modifier.padding(horizontal = Dimens.screenMargin),
        )

        Column(
            modifier = Modifier.padding(horizontal = Dimens.screenMargin),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SerieField(
                value = state.libraryQuery,
                onValueChange = viewModel::setLibraryQuery,
                placeholder = "Buscar exercício",
                leadingIcon = Ph.magnifyingGlass,
            )
            // Sete grupos nao cabem em 360 dp: a faixa rola na horizontal em
            // vez de espremer os rotulos.
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                SerieSegmented(
                    options = viewModel.libraryGroups,
                    selectedIndex = state.libraryGroup,
                    onSelect = viewModel::setLibraryGroup,
                )
            }
        }

        if (results.isEmpty()) {
            EmptyState(
                icon = Ph.magnifyingGlass,
                title = "Nada para “${state.libraryQuery}”",
                body = "Confira a grafia ou crie um exercício personalizado.",
                ctaLabel = "Criar exercício",
                onCta = {},
                modifier = Modifier.padding(top = 48.dp),
            )
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = Dimens.screenMargin,
                    end = Dimens.screenMargin,
                    top = Dimens.blockGap,
                    bottom = Dimens.blockGapLarge,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(results, key = { it.id }) { exercise ->
                    LibraryRow(
                        exercise = exercise,
                        picked = exercise.id in state.libraryPicked,
                        onToggle = { viewModel.toggleLibraryPick(exercise.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryRow(
    exercise: LibraryExercise,
    picked: Boolean,
    onToggle: () -> Unit,
) {
    val c = Nocturne.colors
    ListRow(
        onClick = onToggle,
        leading = {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(Radius.md)
                    .background(c.neutral.s800),
                contentAlignment = Alignment.Center,
            ) {
                PhIcon(iconFor(exercise.group), size = 18.dp, tint = c.textSecondary)
            }
        },
        trailing = {
            // A marca de selecao e um circulo que preenche com o acento ao
            // ser marcado, e fica so como contorno quando vazio.
            Box(
                Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (picked) c.accent else c.bg)
                    .border(
                        Dimens.hairline,
                        if (picked) c.accent else c.divider,
                        RoundedCornerShape(50),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (picked) PhIcon(Ph.check, size = 13.dp, tint = c.accentRamp.s100)
            }
        },
    ) {
        Column {
            Text(exercise.name, style = Nocturne.type.itemTitle, color = c.text)
            Text(
                "${exercise.group.label} · ${exercise.equipment}",
                style = Nocturne.type.caption,
                color = c.textMuted,
            )
        }
    }
}

/** O prototipo usa o halter para quase tudo e silhuetas para pernas e core. */
private fun iconFor(group: MuscleGroup) = when (group) {
    MuscleGroup.Pernas -> Ph.personSimpleWalk
    MuscleGroup.Core -> Ph.personSimple
    else -> Ph.barbell
}
