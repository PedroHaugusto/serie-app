package com.serie.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Tokens de cor do Nocturne.
 *
 * As rampas 100-900 sao invertidas entre os temas: no escuro o passo 100 e
 * claro e o 900 e escuro, no claro acontece o oposto. Por isso um componente
 * pode dizer "preenche com 800, escreve com 100" e funcionar nos dois temas
 * sem nenhum condicional - e a regra que o handoff do prototipo define.
 */
@Immutable
data class NocturneColors(
    val bg: Color,
    val surface: Color,
    val text: Color,
    val accent: Color,
    val accent2: Color,
    val divider: Color,
    val neutral: Ramp,
    val accentRamp: Ramp,
    val accent2Ramp: Ramp,
    val shadowHairline: Color,
    val shadowAmbient: Color,
    val isLight: Boolean,
) {
    /** Uma rampa tonal de 9 passos, do 100 ao 900. */
    @Immutable
    data class Ramp(
        val s100: Color,
        val s200: Color,
        val s300: Color,
        val s400: Color,
        val s500: Color,
        val s600: Color,
        val s700: Color,
        val s800: Color,
        val s900: Color,
    )

    /** Texto secundario: corpo, descricoes. */
    val textSecondary: Color get() = neutral.s300

    /** Texto terciario: metadados, legendas. */
    val textMuted: Color get() = neutral.s400
}

private val DarkNeutral = NocturneColors.Ramp(
    s100 = Color(0xFFF3F5FE), s200 = Color(0xFFE4E7F5), s300 = Color(0xFFCFD3E5),
    s400 = Color(0xFFB2B6CA), s500 = Color(0xFF9397AB), s600 = Color(0xFF75798C),
    s700 = Color(0xFF595D6C), s800 = Color(0xFF3F424D), s900 = Color(0xFF292B31),
)

private val DarkAccent = NocturneColors.Ramp(
    s100 = Color(0xFFF5F4FF), s200 = Color(0xFFE7E5FE), s300 = Color(0xFFD2CEFD),
    s400 = Color(0xFFB5ABFC), s500 = Color(0xFF968AE0), s600 = Color(0xFF796CBF),
    s700 = Color(0xFF5D5294), s800 = Color(0xFF423A6A), s900 = Color(0xFF2B2741),
)

private val DarkAccent2 = NocturneColors.Ramp(
    s100 = Color(0xFFF5F4FF), s200 = Color(0xFFE7E5FE), s300 = Color(0xFFD2CEFD),
    s400 = Color(0xFFB5AFE8), s500 = Color(0xFF9690C9), s600 = Color(0xFF7972A9),
    s700 = Color(0xFF5C5783), s800 = Color(0xFF423E5D), s900 = Color(0xFF2B293A),
)

/** A inversao citada acima: a rampa clara e a escura lida de tras para frente. */
private fun NocturneColors.Ramp.reversed() = NocturneColors.Ramp(
    s100 = s900, s200 = s800, s300 = s700, s400 = s600, s500 = s500,
    s600 = s400, s700 = s300, s800 = s200, s900 = s100,
)

val NocturneDark = NocturneColors(
    bg = Color(0xFF161826),
    surface = Color(0xFF232532),
    text = Color(0xFFE9E9ED),
    accent = Color(0xFF9184D9),
    accent2 = Color(0xFFA7A1DB),
    divider = Color(0xFFE9E9ED).copy(alpha = 0.16f),
    neutral = DarkNeutral,
    accentRamp = DarkAccent,
    accent2Ramp = DarkAccent2,
    shadowHairline = Color(0xFF3F424D),
    shadowAmbient = Color(0xFF000000),
    isLight = false,
)

/**
 * O tema claro nao e uma rampa nova: o fundo e o antigo neutral-200, a
 * superficie o neutral-100, e o acento desce para o passo 600 da rampa de
 * acento. O unico ajuste manual e o neutral-400 (#686C7D), que o prototipo
 * afasta do passo espelhado para manter contraste de legenda sobre fundo claro.
 */
val NocturneLight = NocturneColors(
    bg = Color(0xFFE4E7F5),
    surface = Color(0xFFF3F5FE),
    text = Color(0xFF292B31),
    accent = Color(0xFF796CBF),
    accent2 = Color(0xFF7972A9),
    divider = Color(0xFF292B31).copy(alpha = 0.16f),
    neutral = DarkNeutral.reversed().copy(s400 = Color(0xFF686C7D)),
    accentRamp = DarkAccent.reversed().copy(s900 = Color(0xFFEBE9FB)),
    accent2Ramp = DarkAccent2.reversed(),
    shadowHairline = Color(0xFFCFD3E5),
    shadowAmbient = Color(0xFF292B31),
    isLight = true,
)

val LocalNocturneColors = staticCompositionLocalOf { NocturneDark }
