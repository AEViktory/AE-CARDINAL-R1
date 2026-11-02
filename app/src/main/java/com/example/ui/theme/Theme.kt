package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkM3ColorScheme = darkColorScheme(
    primary = DarkCardinalPalette.cardinalBrightRed,
    onPrimary = DarkCardinalPalette.textPrimary,
    primaryContainer = DarkCardinalPalette.cardinalCrimson,
    secondary = DarkCardinalPalette.celestialGold,
    tertiary = DarkCardinalPalette.astralCyan,
    background = DarkCardinalPalette.background,
    surface = DarkCardinalPalette.surface,
    onBackground = DarkCardinalPalette.textPrimary,
    onSurface = DarkCardinalPalette.textPrimary
)

private val LightM3ColorScheme = lightColorScheme(
    primary = LightCardinalPalette.cardinalCrimson,
    onPrimary = LightCardinalPalette.surface,
    primaryContainer = LightCardinalPalette.cardinalBrightRed,
    secondary = LightCardinalPalette.celestialGold,
    tertiary = LightCardinalPalette.astralCyan,
    background = LightCardinalPalette.background,
    surface = LightCardinalPalette.surface,
    onBackground = LightCardinalPalette.textPrimary,
    onSurface = LightCardinalPalette.textPrimary
)

@Composable
fun AetherealCardinalTheme(
    darkTheme: Boolean = true, // Default to sleek dark cyber aesthetic
    content: @Composable () -> Unit
) {
    val cardinalColors = if (darkTheme) DarkCardinalPalette else LightCardinalPalette
    val m3Colors = if (darkTheme) DarkM3ColorScheme else LightM3ColorScheme

    CompositionLocalProvider(LocalCardinalColors provides cardinalColors) {
        MaterialTheme(
            colorScheme = m3Colors,
            typography = Typography,
            content = content
        )
    }
}
