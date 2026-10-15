package com.serie.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle

/** Ponto unico de acesso aos tokens: `Nocturne.colors`, `Nocturne.type`. */
object Nocturne {
    val colors: NocturneColors
        @Composable @ReadOnlyComposable get() = LocalNocturneColors.current

    val type: NocturneTypography
        @Composable @ReadOnlyComposable get() = LocalNocturneType.current
}

@Composable
fun NocturneTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (dark) NocturneDark else NocturneLight

    // O MaterialTheme continua abaixo porque componentes do Material 3 que
    // usamos (TextField, ripple, seletor de texto) leem o ColorScheme dele.
    // Mapeamos so o que esses componentes consultam; o visual proprio vem
    // sempre dos tokens Nocturne.
    val scheme = remember(colors) {
        val base = if (dark) darkColorScheme() else lightColorScheme()
        base.copy(
            primary = colors.accent,
            onPrimary = colors.accentRamp.s100,
            background = colors.bg,
            onBackground = colors.text,
            surface = colors.surface,
            onSurface = colors.text,
            outline = colors.divider,
            error = colors.accent2,
        )
    }

    CompositionLocalProvider(
        LocalNocturneColors provides colors,
        LocalNocturneType provides NocturneType,
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = MaterialTheme.typography.copy(
                bodyLarge = NocturneType.body,
                bodyMedium = NocturneType.bodySmall,
                labelLarge = NocturneType.button,
            ),
            content = content,
        )
    }
}

/** Atalho para aplicar uma cor a um estilo sem repetir `copy` nas telas. */
fun TextStyle.on(color: androidx.compose.ui.graphics.Color): TextStyle =
    copy(color = color)
