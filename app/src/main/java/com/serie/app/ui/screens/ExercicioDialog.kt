package com.serie.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import com.serie.app.domain.LibraryExercise
import com.serie.app.domain.MuscleGroup
import com.serie.app.ui.components.SerieButton
import com.serie.app.ui.components.SerieButtonStyle
import com.serie.app.ui.components.SerieField
import com.serie.app.ui.components.SerieSegmented
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.Radius
import com.serie.app.ui.theme.elevLg
import com.serie.app.ui.theme.elevSm

/**
 * Criacao e edicao de exercicio.
 *
 * Series, reps e carga continuam de fora: sao prescricao do treino, nao
 * propriedade do exercicio - o mesmo supino aparece com cargas diferentes em
 * dois treinos. Ja a descricao e do exercicio, porque uma dica de execucao
 * vale em qualquer treino onde ele apareca.
 */
@Composable
fun ExercicioDialog(
    draft: LibraryExercise,
    isNew: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSave: (name: String, group: MuscleGroup, equipment: String, notes: String) -> Unit,
) {
    val c = Nocturne.colors
    val groups = MuscleGroup.entries
    var name by remember(draft.id) { mutableStateOf(draft.name) }
    var equipment by remember(draft.id) { mutableStateOf(draft.equipment) }
    var notes by remember(draft.id) { mutableStateOf(draft.notes) }
    var groupIndex by remember(draft.id) { mutableIntStateOf(groups.indexOf(draft.group).coerceAtLeast(0)) }

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
            Text(
                if (isNew) "Novo exercício" else "Editar exercício",
                style = Nocturne.type.h2,
                color = c.text,
            )

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

            SerieField(
                value = notes,
                onValueChange = { notes = it },
                label = "Descrição",
                placeholder = "Cotovelos junto ao corpo, descer devagar…",
                singleLine = false,
                modifier = Modifier.heightIn(min = 88.dp),
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
                    label = if (isNew) "Criar" else "Salvar",
                    onClick = { onSave(name, groups[groupIndex], equipment, notes) },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
