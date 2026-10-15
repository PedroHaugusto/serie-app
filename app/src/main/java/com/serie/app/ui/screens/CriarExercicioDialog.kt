package com.serie.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.serie.app.domain.MuscleGroup
import com.serie.app.ui.components.SerieButton
import com.serie.app.ui.components.SerieButtonStyle
import com.serie.app.ui.components.SerieField
import com.serie.app.ui.components.SerieSegmented
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.Radius
import com.serie.app.ui.theme.elevLg

/**
 * Criacao de exercicio personalizado.
 *
 * Pede so o minimo que a biblioteca precisa para listar e filtrar: nome,
 * grupo e equipamento. Series, reps e carga ficam de fora de proposito - sao
 * prescricao do treino, nao propriedade do exercicio, e o mesmo supino pode
 * aparecer com cargas diferentes em dois treinos.
 */
@Composable
fun CriarExercicioDialog(
    error: String?,
    onDismiss: () -> Unit,
    onCreate: (name: String, group: MuscleGroup, equipment: String) -> Unit,
) {
    val c = Nocturne.colors
    var name by remember { mutableStateOf("") }
    var equipment by remember { mutableStateOf("") }
    var groupIndex by remember { mutableIntStateOf(0) }
    val groups = MuscleGroup.entries

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Radius.lg)
                .background(c.surface)
                .elevLg(Radius.lg)
                .padding(Dimens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.blockGap),
        ) {
            Text("Novo exercício", style = Nocturne.type.h2, color = c.text)

            SerieField(
                value = name,
                onValueChange = { name = it },
                label = "Nome",
                placeholder = "Crucifixo inclinado",
            )

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    "Grupo muscular",
                    style = Nocturne.type.label,
                    color = c.text.copy(alpha = 0.70f),
                )
                // Seis grupos nao cabem em 360 dp menos a margem do dialogo.
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    SerieSegmented(
                        options = groups.map { it.label },
                        selectedIndex = groupIndex,
                        onSelect = { groupIndex = it },
                    )
                }
            }

            SerieField(
                value = equipment,
                onValueChange = { equipment = it },
                label = "Equipamento",
                placeholder = "Halteres, barra, polia…",
            )

            if (error != null) {
                Text(error, style = Nocturne.type.caption, color = c.accent2)
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SerieButton(
                    label = "Cancelar",
                    onClick = onDismiss,
                    style = SerieButtonStyle.Secondary,
                    modifier = Modifier.weight(1f),
                )
                SerieButton(
                    label = "Criar",
                    onClick = { onCreate(name, groups[groupIndex], equipment) },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
