package com.serie.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.serie.app.R
import com.serie.app.ui.theme.Nocturne

val PhosphorRegular = FontFamily(Font(R.font.phosphor_regular))
val PhosphorFill = FontFamily(Font(R.font.phosphor_fill))

/**
 * Desenha um icone Phosphor.
 *
 * O glifo e texto, entao herda cor e tamanho como texto - mas texto tem
 * entrelinha propria, que deslocaria o icone dentro da linha. Por isso a altura
 * de linha e fixada no proprio tamanho e o glifo e centralizado numa caixa
 * quadrada: assim o icone ocupa exatamente a area que o `size` promete.
 *
 * O tamanho vem em dp e e convertido para sp sem passar pela escala de fonte
 * do sistema: um icone de 22 dp precisa medir 22 dp mesmo quando o usuario
 * aumenta o corpo do texto, senao estoura a barra de abas.
 */
@Composable
fun PhIcon(
    icon: PhIcon,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    tint: Color = Nocturne.colors.text,
    filled: Boolean = false,
) {
    val density = LocalDensity.current
    val fontSize = with(density) { size.toPx().toSp() }
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Text(
            text = icon.glyph,
            color = tint,
            style = TextStyle(
                fontFamily = if (filled) PhosphorFill else PhosphorRegular,
                fontSize = fontSize,
                lineHeight = fontSize,
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both,
                ),
            ),
        )
    }
}
