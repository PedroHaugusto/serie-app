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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.serie.app.data.SeedData
import com.serie.app.ui.SerieUiState
import com.serie.app.ui.SerieViewModel
import com.serie.app.ui.components.ListRow
import com.serie.app.ui.components.Ph
import com.serie.app.ui.components.PhIcon
import com.serie.app.ui.components.SectionLabel
import com.serie.app.ui.components.SerieCard
import com.serie.app.ui.components.SerieSegmented
import com.serie.app.ui.components.SerieTag
import com.serie.app.ui.components.SerieTagStyle
import com.serie.app.ui.components.VerticalHairline
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.Radius

/** 10 · Perfil e configuracoes. */
@Composable
fun PerfilScreen(
    state: SerieUiState,
    viewModel: SerieViewModel,
    modifier: Modifier = Modifier,
) {
    val c = Nocturne.colors
    val profile = SeedData.profile
    val prefs = state.preferences

    LazyColumn(
        modifier = modifier.fillMaxSize().background(c.bg),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = Dimens.screenMargin,
            end = Dimens.screenMargin,
            top = Dimens.screenMargin,
            bottom = Dimens.blockGapLarge,
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.blockGap),
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(50))
                        .background(c.accentRamp.s900),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(profile.initial, style = Nocturne.type.h1, color = c.accent)
                }
                Column {
                    Text(profile.name, style = Nocturne.type.h2, color = c.text)
                    Text(profile.since, style = Nocturne.type.caption, color = c.textMuted)
                }
            }
        }

        item {
            SerieCard(padding = Dimens.cardPaddingTight) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    ProfileStat("${profile.workouts}", "treinos", Modifier.weight(1f))
                    VerticalHairline(height = 40.dp)
                    ProfileStat("${profile.streak}", "dias seguidos", Modifier.weight(1f))
                    VerticalHairline(height = 40.dp)
                    ProfileStat("${profile.records}", "recordes", Modifier.weight(1f))
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionLabel("Meta semanal")
                SerieSegmented(
                    options = listOf("3×", "4×", "5×", "6×"),
                    selectedIndex = (prefs.weeklyGoal - 3).coerceIn(0, 3),
                    onSelect = viewModel::setWeeklyGoal,
                    fillWidth = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        item { SectionLabel("Preferências") }

        item {
            PreferenceRow(icon = Ph.scales, label = "Unidade") {
                SerieSegmented(
                    options = listOf("kg", "lb"),
                    selectedIndex = if (prefs.useKilograms) 0 else 1,
                    onSelect = viewModel::setUnit,
                )
            }
        }

        item {
            PreferenceRow(icon = Ph.moon, label = "Tema") {
                SerieSegmented(
                    options = listOf("Escuro", "Claro"),
                    selectedIndex = if (prefs.darkTheme) 0 else 1,
                    onSelect = { viewModel.setDarkTheme(it == 0) },
                )
            }
        }

        item {
            PreferenceRow(
                icon = Ph.bell,
                label = "Lembretes de treino",
                sublabel = prefs.reminderLabel,
            ) {
                Switch(checked = prefs.remindersOn, onToggle = viewModel::toggleReminders)
            }
        }

        item { SectionLabel("Conexões") }

        item {
            ListRow(
                leading = { PrefIcon(Ph.heartbeat) },
                trailing = { SerieTag("Conectado", style = SerieTagStyle.Accent) },
            ) {
                Text("Health Connect", style = Nocturne.type.itemTitle, color = c.text)
            }
        }

        item {
            ListRow(
                onClick = {},
                leading = { PrefIcon(Ph.watch) },
                trailing = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("Parear", style = Nocturne.type.segment, color = c.accent)
                        PhIcon(Ph.caretRight, size = 14.dp, tint = c.textMuted)
                    }
                },
            ) {
                Text("Wear OS", style = Nocturne.type.itemTitle, color = c.text)
            }
        }

        item {
            ListRow(
                onClick = {},
                leading = { PrefIcon(Ph.export) },
                trailing = { PhIcon(Ph.caretRight, size = 14.dp, tint = c.textMuted) },
            ) {
                Text("Exportar dados (CSV)", style = Nocturne.type.itemTitle, color = c.text)
            }
        }

        item {
            Row(
                modifier = Modifier
                    .clip(Radius.md)
                    .clickable {}
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PhIcon(Ph.signOut, size = 16.dp, tint = c.accent2)
                Text("Sair", style = Nocturne.type.button, color = c.accent2)
            }
        }
    }
}

@Composable
private fun ProfileStat(value: String, caption: String, modifier: Modifier = Modifier) {
    val c = Nocturne.colors
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(value, style = Nocturne.type.h2, color = c.text)
        Text(caption, style = Nocturne.type.caption, color = c.textMuted)
    }
}

@Composable
private fun PrefIcon(icon: PhIcon) {
    val c = Nocturne.colors
    Box(
        Modifier
            .size(36.dp)
            .clip(Radius.md)
            .background(c.neutral.s800),
        contentAlignment = Alignment.Center,
    ) { PhIcon(icon, size = 18.dp, tint = c.textSecondary) }
}

@Composable
private fun PreferenceRow(
    icon: PhIcon,
    label: String,
    sublabel: String? = null,
    control: @Composable () -> Unit,
) {
    val c = Nocturne.colors
    ListRow(leading = { PrefIcon(icon) }, trailing = control) {
        Column {
            Text(label, style = Nocturne.type.itemTitle, color = c.text)
            sublabel?.let {
                Text(it, style = Nocturne.type.caption, color = c.textMuted)
            }
        }
    }
}

/**
 * Interruptor no desenho do Nocturne: trilho de 44 x 26 com um disco que
 * desliza. O Material tem um Switch pronto, mas ele traz a paleta propria e
 * ficaria deslocado ao lado dos seletores segmentados desta tela.
 */
@Composable
private fun Switch(checked: Boolean, onToggle: () -> Unit) {
    val c = Nocturne.colors
    Box(
        modifier = Modifier
            .size(width = 44.dp, height = 26.dp)
            .clip(RoundedCornerShape(50))
            .background(if (checked) c.accent else c.neutral.s800)
            .clickable(onClick = onToggle),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .padding(horizontal = 3.dp)
                .size(20.dp)
                .clip(RoundedCornerShape(50))
                .background(if (checked) c.accentRamp.s100 else c.neutral.s500)
        )
    }
}
