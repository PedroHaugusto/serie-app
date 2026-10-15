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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.serie.app.domain.UserProfile
import com.serie.app.ui.components.SerieButton
import com.serie.app.ui.components.SerieButtonStyle
import com.serie.app.ui.components.SerieField
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.Radius
import com.serie.app.ui.theme.elevLg
import com.serie.app.ui.theme.elevSm

/**
 * Edicao do perfil.
 *
 * Só nome e data de inicio. Treinos, sequencia e recordes sao derivados do
 * historico: deixar o usuario digita-los transformaria estatistica em enfeite.
 */
@Composable
fun EditarPerfilDialog(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (name: String, since: String) -> Unit,
) {
    val c = Nocturne.colors
    var name by remember { mutableStateOf(profile.name) }
    var since by remember { mutableStateOf(profile.since) }

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
            Text("Editar perfil", style = Nocturne.type.h2, color = c.text)

            SerieField(
                value = name,
                onValueChange = { name = it },
                label = "Nome",
                placeholder = "Seu nome",
            )
            Text(
                "A inicial do avatar vem daqui.",
                style = Nocturne.type.caption,
                color = c.textMuted,
            )

            SerieField(
                value = since,
                onValueChange = { since = it },
                label = "Treinando desde",
                placeholder = "março de 2026",
            )

            Row(
                Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SerieButton(
                    label = "Cancelar",
                    onClick = onDismiss,
                    style = SerieButtonStyle.Secondary,
                    modifier = Modifier.weight(1f),
                )
                SerieButton(
                    label = "Salvar",
                    onClick = { onSave(name, since) },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
