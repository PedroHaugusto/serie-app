package com.serie.app.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.serie.app.domain.CsvExport
import com.serie.app.domain.DateFormat
import com.serie.app.domain.plural
import com.serie.app.domain.Num
import com.serie.app.export.CsvSharing
import com.serie.app.notify.ReminderScheduler
import com.serie.app.ui.SerieUiState
import com.serie.app.ui.SerieViewModel
import com.serie.app.ui.components.ListRow
import com.serie.app.ui.components.Ph
import com.serie.app.ui.components.PhIcon
import com.serie.app.ui.components.SectionLabel
import com.serie.app.ui.components.SerieCard
import com.serie.app.ui.components.SerieSegmented
import com.serie.app.ui.components.VerticalHairline
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.Radius
import com.serie.app.ui.theme.elevSm

/** 10 · Perfil e configuracoes. */
@Composable
fun PerfilScreen(
    state: SerieUiState,
    viewModel: SerieViewModel,
    modifier: Modifier = Modifier,
) {
    val c = Nocturne.colors
    val context = LocalContext.current
    val profile = state.profile
    val prefs = state.preferences

    var notifyDenied by remember { mutableStateOf(false) }
    val askNotify = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        notifyDenied = !granted
        if (granted) viewModel.setReminders(true)
    }

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
                Modifier
                    .fillMaxWidth()
                    .clip(Radius.lg)
                    .clickable(onClick = viewModel::openEditProfile)
                    .padding(vertical = 4.dp),
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
                Column(Modifier.weight(1f)) {
                    Text(profile.name, style = Nocturne.type.h2, color = c.text)
                    Text(
                        "Treinando desde ${profile.since}",
                        style = Nocturne.type.caption,
                        color = c.textMuted,
                    )
                }
                Text("Editar", style = Nocturne.type.segment, color = c.accent)
            }
        }

        item {
            SerieCard(padding = Dimens.cardPaddingTight) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    // "Dias seguidos" saiu daqui pelo mesmo motivo que saiu da
                    // tela Hoje: quem treina em dias alternados nunca passa de
                    // um, e o numero virava uma cobranca sem significado.
                    ProfileStat(Num.integer(state.totalWorkouts), "treinos", Modifier.weight(1f))
                    VerticalHairline(height = 40.dp)
                    ProfileStat(Num.integer(state.totalSets), "séries", Modifier.weight(1f))
                    VerticalHairline(height = 40.dp)
                    ProfileStat(Num.integer(state.totalRecords), "recordes", Modifier.weight(1f))
                }
            }
        }

        item { SectionLabel("Preferências") }

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
                label = "Lembrete de treino",
                sublabel = if (prefs.remindersOn) prefs.reminderLabel else "desligado",
            ) {
                Switch(
                    checked = prefs.remindersOn,
                    onToggle = {
                        when {
                            prefs.remindersOn -> viewModel.setReminders(false)
                            ReminderScheduler.canNotify(context) -> viewModel.setReminders(true)
                            // Sem a permissao o alarme dispararia e a
                            // notificacao seria descartada em silencio, o que
                            // deixaria o interruptor ligado prometendo nada.
                            else -> askNotify.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                )
            }
        }

        if (prefs.remindersOn) {
            item {
                SerieCard(padding = Dimens.cardPaddingTight) {
                    Text("Dias", style = Nocturne.type.label, color = c.text.copy(alpha = 0.7f))
                    Row(
                        Modifier.fillMaxWidth().padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        DateFormat.WEEKDAY_INITIALS.forEachIndexed { i, label ->
                            DayChip(
                                label = label,
                                on = prefs.reminderDays.getOrNull(i) == true,
                                onClick = { viewModel.toggleReminderDay(i) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(Radius.md)
                            .clickable(onClick = viewModel::openReminderTime)
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Horário",
                            style = Nocturne.type.itemTitle,
                            color = c.text,
                            modifier = Modifier.weight(1f),
                        )
                        Text(prefs.timeLabel, style = Nocturne.type.h2, color = c.accent)
                        PhIcon(Ph.caretRight, size = 14.dp, tint = c.textMuted)
                    }
                    when {
                        prefs.reminderDays.none { it } ->
                            Hint("Escolha ao menos um dia — sem dia, nada é avisado.")
                        !ReminderScheduler.canNotify(context) -> Hint(
                            "As notificações da série estão bloqueadas nos ajustes " +
                                "do Android, então o aviso não vai aparecer.",
                        )
                        // Dito aqui porque o atraso e real e nao tem conserto
                        // sem a permissao que o Android reserva a despertadores.
                        else -> Hint(
                            "O aviso pode chegar alguns minutos depois se o " +
                                "celular estiver dormindo.",
                        )
                    }
                }
            }
        } else if (notifyDenied) {
            item {
                Hint(
                    "Sem permissão para notificar, o lembrete não teria como " +
                        "aparecer. Você pode liberá-la nos ajustes do Android.",
                )
            }
        }

        item { SectionLabel("Dados") }

        item {
            val hasData = state.history.isNotEmpty() ||
                state.bodyWeights.isNotEmpty() ||
                state.measureEntries.isNotEmpty()
            ListRow(
                // Sem nada registrado a linha nao e clicavel: tres arquivos
                // so com cabecalho nao sao um backup.
                onClick = if (hasData) {
                    {
                        CsvSharing.share(
                            context,
                            CsvExport.documents(
                                history = state.history,
                                weights = state.bodyWeights,
                                measureTypes = state.measureTypes,
                                measureEntries = state.measureEntries,
                            ),
                        )
                    }
                } else {
                    null
                },
                leading = { PrefIcon(Ph.export) },
                trailing = {
                    if (hasData) PhIcon(Ph.caretRight, size = 14.dp, tint = c.textMuted)
                },
            ) {
                Column {
                    Text(
                        "Exportar dados (CSV)",
                        style = Nocturne.type.itemTitle,
                        color = if (hasData) c.text else c.textMuted,
                    )
                    Text(
                        if (hasData) {
                            plural(state.history.size, "treino") + " · " +
                                plural(state.bodyWeights.size, "pesagem") + " · " +
                                plural(state.measureEntries.size, "medida")
                        } else {
                            "nada registrado ainda"
                        },
                        style = Nocturne.type.caption,
                        color = c.textMuted,
                    )
                }
            }
        }
    }

    if (state.editingProfile) {
        EditarPerfilDialog(
            profile = profile,
            onDismiss = viewModel::dismissEditProfile,
            onSave = viewModel::saveProfile,
        )
    }

    if (state.editingReminderTime) {
        HorarioDialog(
            hour = prefs.reminderHour,
            minute = prefs.reminderMinute,
            onDismiss = viewModel::dismissReminderTime,
            onSave = viewModel::setReminderTime,
        )
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
private fun Hint(text: String) {
    Text(
        text,
        style = Nocturne.type.caption,
        color = Nocturne.colors.textMuted,
        modifier = Modifier.padding(top = 4.dp),
    )
}

/** O disco de um dia da semana, aceso quando o dia esta marcado. */
@Composable
private fun DayChip(
    label: String,
    on: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Nocturne.colors
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(50))
            .background(if (on) c.accentRamp.s900 else c.bg)
            .then(if (!on) Modifier.elevSm(RoundedCornerShape(50)) else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = Nocturne.type.itemTitle,
            color = if (on) c.accent else c.textMuted,
        )
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
