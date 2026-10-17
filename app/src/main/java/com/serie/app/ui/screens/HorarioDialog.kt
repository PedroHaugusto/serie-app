package com.serie.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.serie.app.ui.components.Ph
import com.serie.app.ui.components.SerieBlockButton
import com.serie.app.ui.components.SerieButtonStyle
import com.serie.app.ui.components.SerieIconButton
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.Radius
import com.serie.app.ui.theme.elevLg
import com.serie.app.ui.theme.elevSm

/** O minuto anda de cinco em cinco: lembrete nao precisa de precisao de minuto. */
private const val MINUTE_STEP = 5

/**
 * Horario do lembrete, em setas em vez de teclado.
 *
 * O seletor do Material resolveria, mas traz a paleta propria e ficaria
 * deslocado - a mesma razao pela qual o interruptor desta tela e desenhado
 * aqui. Setas tambem evitam o unico caminho em que digitar erra: "7" sozinho
 * no campo de hora nao distingue 07:00 de um 7 que o usuario ainda ia
 * completar.
 */
@Composable
fun HorarioDialog(
    hour: Int,
    minute: Int,
    onDismiss: () -> Unit,
    onSave: (hour: Int, minute: Int) -> Unit,
) {
    val c = Nocturne.colors
    var h by remember { mutableIntStateOf(hour) }
    var m by remember { mutableIntStateOf(minute) }

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
            Text("Horário do lembrete", style = Nocturne.type.h2, color = c.text)

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                // O relogio da volta: subir de 23 chega a 00, e nao para -
                // quem passou do horario nao precisa descer vinte e tres vezes.
                Stepper(
                    value = h,
                    label = "hora",
                    onUp = { h = (h + 1) % 24 },
                    onDown = { h = (h + 23) % 24 },
                )
                Text(
                    ":",
                    style = Nocturne.type.display,
                    color = c.textMuted,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                Stepper(
                    value = m,
                    label = "minuto",
                    onUp = { m = (m + MINUTE_STEP) % 60 },
                    onDown = { m = (m + 60 - MINUTE_STEP) % 60 },
                )
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SerieBlockButton(
                    label = "Cancelar",
                    onClick = onDismiss,
                    style = SerieButtonStyle.Secondary,
                    modifier = Modifier.weight(1f),
                )
                SerieBlockButton(
                    label = "Salvar",
                    onClick = { onSave(h, m) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun Stepper(
    value: Int,
    label: String,
    onUp: () -> Unit,
    onDown: () -> Unit,
) {
    val c = Nocturne.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SerieIconButton(icon = Ph.caretUp, onClick = onUp, tint = c.accent)
        Text(
            "%02d".format(value),
            style = Nocturne.type.display,
            color = c.text,
            textAlign = TextAlign.Center,
        )
        SerieIconButton(icon = Ph.caretDown, onClick = onDown, tint = c.accent)
        Text(label, style = Nocturne.type.caption, color = c.textMuted)
    }
}
