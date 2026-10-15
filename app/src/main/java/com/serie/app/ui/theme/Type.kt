package com.serie.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.serie.app.R

/**
 * Inter com peso maximo 500: o Nocturne nunca usa bold. A hierarquia vem do
 * tamanho, do espaco e do tracking negativo, que aperta conforme o texto
 * cresce (-3.5% no display, -2.5% no H1, -1% no H2, zero no corpo).
 */
val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
)

/** Numeros de largura fixa, para que placares e cargas nao dancem ao mudar. */
private const val TABULAR = "tnum"

@Immutable
data class NocturneTypography(
    /** 40/500 -3.5% - numeros grandes: contagem de serie, volume total. */
    val display: TextStyle,
    /** 26-28/500 -2.5% - titulo de tela. */
    val h1: TextStyle,
    /** 20/500 -1% - titulo de secao e nome de treino. */
    val h2: TextStyle,
    /** 15/500 - titulo de item de lista. */
    val itemTitle: TextStyle,
    /** 14/400 - corpo. */
    val body: TextStyle,
    /** 13/400 - corpo denso, o padrao das telas. */
    val bodySmall: TextStyle,
    /** 12/400 - rotulo de campo. */
    val label: TextStyle,
    /** 11/400 - legenda e meta. */
    val caption: TextStyle,
    /** 11/500 +10% caixa alta - sobretitulo de card. */
    val kicker: TextStyle,
    /** 15/500 - texto de botao. */
    val button: TextStyle,
    /** 13/500 - opcao de seletor segmentado. */
    val segment: TextStyle,
)

val NocturneType = NocturneTypography(
    display = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Medium,
        fontSize = 40.sp, lineHeight = 40.sp, letterSpacing = (-0.035).em,
        fontFeatureSettings = TABULAR,
    ),
    h1 = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Medium,
        fontSize = 28.sp, lineHeight = 32.sp, letterSpacing = (-0.025).em,
    ),
    h2 = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Medium,
        fontSize = 20.sp, lineHeight = 24.sp, letterSpacing = (-0.01).em,
    ),
    itemTitle = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Medium,
        fontSize = 15.sp, lineHeight = 20.sp,
    ),
    body = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Normal,
        fontSize = 13.sp, lineHeight = 18.sp,
    ),
    label = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp,
    ),
    caption = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Normal,
        fontSize = 11.sp, lineHeight = 14.sp,
    ),
    kicker = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.1.em,
    ),
    button = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Medium,
        fontSize = 15.sp, lineHeight = 20.sp,
    ),
    segment = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Medium,
        fontSize = 13.sp, lineHeight = 16.sp,
    ),
)

val LocalNocturneType = staticCompositionLocalOf { NocturneType }
