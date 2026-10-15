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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.serie.app.ui.components.SerieBlockButton
import com.serie.app.ui.components.SerieButtonStyle
import com.serie.app.ui.components.SerieField
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.Radius
import com.serie.app.ui.theme.elevLg
import com.serie.app.ui.theme.elevSm

/**
 * Registro da pesagem de hoje.
 *
 * Vive fora das telas porque e chamado de duas - da metrica na Hoje e do
 * "Registrar" em Corpo - e duas copias divergiriam na primeira mudanca.
 */
@Composable
fun RegistrarPesoDialog(
    current: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    val c = Nocturne.colors
    var text by remember { mutableStateOf(current) }

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
            Text("Peso de hoje", style = Nocturne.type.h2, color = c.text)
            SerieField(
                value = text,
                onValueChange = { text = it },
                label = "Peso (kg)",
                placeholder = "78,4",
                keyboardType = KeyboardType.Decimal,
            )
            Text(
                "Pesar de novo hoje corrige o valor em vez de criar outro registro.",
                style = Nocturne.type.caption,
                color = c.textMuted,
            )
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
                    onClick = { onSave(text) },
                    enabled = text.isNotBlank(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
