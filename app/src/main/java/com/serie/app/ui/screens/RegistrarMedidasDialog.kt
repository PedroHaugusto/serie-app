package com.serie.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.serie.app.domain.MeasureRow
import com.serie.app.domain.Stats
import com.serie.app.ui.components.Ph
import com.serie.app.ui.components.PhIcon
import com.serie.app.ui.components.SerieBlockButton
import com.serie.app.ui.components.SerieButtonStyle
import com.serie.app.ui.components.SerieField
import com.serie.app.ui.components.SerieSegmented
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.Radius
import com.serie.app.ui.theme.elevLg
import com.serie.app.ui.theme.elevSm

/**
 * Registro das medidas de hoje.
 *
 * Todas as medidas num dialogo so, e nao uma de cada vez: quem pega a fita
 * metrica mede tudo na mesma sessao, e abrir quatro dialogos para isso
 * transformaria um gesto em quatro.
 */
@Composable
fun RegistrarMedidasDialog(
    rows: List<MeasureRow>,
    onDismiss: () -> Unit,
    onSave: (Map<String, String>) -> Unit,
    onCreate: () -> Unit,
    onDelete: (String) -> Unit,
) {
    val c = Nocturne.colors
    // Comeca vazio em vez de trazer a ultima medicao: campo preenchido com o
    // valor antigo vira registro novo igual ao anterior se o usuario so
    // confirmar, e a tabela acusaria "sem variacao" sem ele ter medido nada.
    val values = remember { mutableStateMapOf<String, String>() }

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
            Text("Medidas de hoje", style = Nocturne.type.h2, color = c.text)

            Column(
                modifier = Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rows.forEach { row ->
                    MeasureField(
                        row = row,
                        value = values[row.type.id].orEmpty(),
                        onValueChange = { values[row.type.id] = it },
                        onDelete = { onDelete(row.type.id) },
                    )
                }
            }

            Row(
                modifier = Modifier
                    .clip(Radius.md)
                    .clickable(onClick = onCreate)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                PhIcon(Ph.plus, size = 14.dp, tint = c.accent)
                Text("Nova medida", style = Nocturne.type.button, color = c.accent)
            }

            Text(
                "Deixe em branco o que você não mediu hoje.",
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
                    onClick = { onSave(values.toMap()) },
                    enabled = values.values.any { it.isNotBlank() },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MeasureField(
    row: MeasureRow,
    value: String,
    onValueChange: (String) -> Unit,
    onDelete: () -> Unit,
) {
    val c = Nocturne.colors
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SerieField(
            value = value,
            onValueChange = onValueChange,
            label = row.type.name,
            // O ultimo valor vira sugestao em vez de conteudo: lembra onde a
            // medida estava sem virar registro por descuido.
            placeholder = row.latest?.let { Stats.formatCm(it.cm) } ?: "cm",
            keyboardType = KeyboardType.Decimal,
            modifier = Modifier.weight(1f),
        )
        if (row.type.isCustom) {
            Row(
                modifier = Modifier
                    .clip(Radius.md)
                    .clickable(onClick = onDelete)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PhIcon(Ph.trash, size = 15.dp, tint = c.accent2)
            }
        }
    }
}

/** Criacao de uma medida do usuario. */
@Composable
fun NovaMedidaDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Boolean) -> Unit,
) {
    val c = Nocturne.colors
    var name by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf(0) }

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
            Text("Nova medida", style = Nocturne.type.h2, color = c.text)
            SerieField(
                value = name,
                onValueChange = { name = it },
                label = "Nome",
                placeholder = "Quadril",
            )
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    "O que você quer que aconteça",
                    style = Nocturne.type.label,
                    color = c.text.copy(alpha = 0.70f),
                )
                SerieSegmented(
                    options = listOf("Crescer", "Diminuir"),
                    selectedIndex = direction,
                    onSelect = { direction = it },
                    fillWidth = true,
                )
            }
            Text(
                // Sem isso a tabela nao sabe pintar a variacao: encolher a
                // cintura e ganho, encolher o braco nao.
                "É o que diz se a variação foi na direção certa.",
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
                    label = "Criar",
                    onClick = { onCreate(name, direction == 1) },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
