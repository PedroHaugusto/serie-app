package com.serie.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.Radius

/**
 * Campo de texto do Nocturne.
 *
 * Usa [BasicTextField] em vez do OutlinedTextField do Material porque o
 * Material desenha rotulo flutuante, contorno entalhado e alturas proprias -
 * tudo o que teriamos de desfazer. O campo daqui e o que o design pede:
 * superficie, fio de divisao, e o acento so quando esta em foco.
 */
@Composable
fun SerieField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: PhIcon? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val c = Nocturne.colors
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val borderColor = if (focused) c.accent else c.divider

    Column(modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        label?.let {
            Text(it, style = Nocturne.type.label, color = c.text.copy(alpha = 0.70f))
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = singleLine,
            textStyle = Nocturne.type.body.copy(color = c.text),
            cursorBrush = SolidColor(c.accent),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            interactionSource = interaction,
            modifier = Modifier
                .fillMaxWidth()
                .clip(Radius.md)
                .background(c.surface)
                .border(Dimens.hairline, borderColor, Radius.md)
                .defaultMinSize(minHeight = Dimens.touchTargetMin)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            decorationBox = { inner ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    leadingIcon?.let { PhIcon(it, size = 18.dp, tint = c.neutral.s400) }
                    Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty() && placeholder != null) {
                            Text(placeholder, style = Nocturne.type.body, color = c.neutral.s500)
                        }
                        inner()
                    }
                    trailingContent?.invoke()
                }
            },
        )
    }
}
