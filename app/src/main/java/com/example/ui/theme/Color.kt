package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * AETHEREAL CARDINAL color palette — high-contrast celestial & crimson aesthetic.
 */
data class CardinalColors(
    val background: Color,
    val surface: Color,
    val cardBackground: Color,
    val borderColor: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val cardinalCrimson: Color,
    val cardinalBrightRed: Color,
    val celestialGold: Color,
    val astralCyan: Color,
    val accentPurple: Color,
    val accentAlert: Color,
    val isDark: Boolean
)

val DarkCardinalPalette = CardinalColors(
    background = Color(0xFF09060E),         // Deep cosmic space obsidian
    surface = Color(0xFF130A19),            // Dark celestial amethyst/charcoal
    cardBackground = Color(0xFF1C1024),     // Elevated card background
    borderColor = Color(0xFF352042),        // Hairline cosmic border
    textPrimary = Color(0xFFF8F9FA),        // Bright star white
    textSecondary = Color(0xFFA69EB0),      // Starlight silver-grey
    cardinalCrimson = Color(0xFFD90429),    // Deep Cardinal crimson
    cardinalBrightRed = Color(0xFFFF2A55),  // Radiant celestial red
    celestialGold = Color(0xFFFFD166),      // Astral gold
    astralCyan = Color(0xFF00F5D4),         // Ethereal quantum cyan
    accentPurple = Color(0xFF9D4EDD),       // Nebula violet
    accentAlert = Color(0xFFEF233C),        // Critical warning red
    isDark = true
)

val LightCardinalPalette = CardinalColors(
    background = Color(0xFFFAF8FB),
    surface = Color(0xFFFFFFFF),
    cardBackground = Color(0xFFFFFFFF),
    borderColor = Color(0xFFE5DEEC),
    textPrimary = Color(0xFF150A1E),
    textSecondary = Color(0xFF6E6478),
    cardinalCrimson = Color(0xFFC9002B),
    cardinalBrightRed = Color(0xFFD90429),
    celestialGold = Color(0xFFE5A100),
    astralCyan = Color(0xFF009688),
    accentPurple = Color(0xFF7209B7),
    accentAlert = Color(0xFFD90429),
    isDark = false
)

val LocalCardinalColors = compositionLocalOf { DarkCardinalPalette }

object CardinalTheme {
    val colors: CardinalColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCardinalColors.current
}
