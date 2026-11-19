package com.opendroid.ai.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * OpenDroid color palette — retro-cyberpunk / neon edition.
 *
 * Main colors: hot fuchsia (#FF00E5) and electric cyan (#00F0FF).
 * Secondary: pure white and black. The palette is resolved via
 * CompositionLocal so all screens automatically adapt to the active
 * theme (dark / light).
 */
data class OpenDroidColors(
    val background: Color,
    val surface: Color,
    val cardBackground: Color,
    val borderColor: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    /** Hot fuchsia — the primary neon accent (thin marks, highlights). */
    val accentFuchsia: Color,
    /** Hot fuchsia for large filled button surfaces (black text on top). */
    val accentFuchsiaButton: Color,
    val accentPurple: Color,
    val accentCyan: Color,
    val accentRed: Color,
    val accentOrange: Color = Color(0xFFFF9500),
    val isDark: Boolean
)

// ── Dark palette: Neon Night ──────────────────────────────────
// Near-black violet background, hot fuchsia + electric cyan neons.
val DarkPalette = OpenDroidColors(
    background = Color(0xFF05050F),      // Near-black with a violet tint
    surface = Color(0xFF0B0B1A),         // Deep violet-black surface
    cardBackground = Color(0xFF12122B),  // Elevated violet card surface
    borderColor = Color(0xFF2A2A4D),     // Dim violet hairline border
    textPrimary = Color(0xFFFFFFFF),     // Pure white
    textSecondary = Color(0xFFB9B9D6),   // Cool lavender gray
    accentFuchsia = Color(0xFFFF00E5),   // Hot fuchsia neon
    accentFuchsiaButton = Color(0xFFFF00E5), // Hot fuchsia button
    accentPurple = Color(0xFFA855F7),    // Electric violet (fuchsia's partner)
    accentCyan = Color(0xFF00F0FF),      // Electric cyan neon
    accentRed = Color(0xFFFF2D55),       // Hot pink-red alert
    accentOrange = Color(0xFFFF9500),
    isDark = true
)

// ── Light palette: Neon Day ───────────────────────────────────
// White terminal: pure white background, black text, the same neons
// darkened just enough to stay readable on white.
val LightPalette = OpenDroidColors(
    background = Color(0xFFFFFFFF),      // Pure white
    surface = Color(0xFFF4F4FA),         // Crisp lavender-white surface
    cardBackground = Color(0xFFFFFFFF),  // Pure white card
    borderColor = Color(0xFFE0E0F0),     // Pale lavender hairline border
    textPrimary = Color(0xFF0A0A14),     // Near-black
    textSecondary = Color(0xFF5A5A72),   // Slate violet
    accentFuchsia = Color(0xFFD100B8),   // Deep fuchsia (readable on white)
    accentFuchsiaButton = Color(0xFFD100B8), // Deep fuchsia button
    accentPurple = Color(0xFF7E22CE),    // Purple
    accentCyan = Color(0xFF0891B2),      // Deep cyan (readable on white)
    accentRed = Color(0xFFDC2626),       // Clean alert red
    accentOrange = Color(0xFFD97706),
    isDark = false
)

val LocalOpenDroidColors = compositionLocalOf { DarkPalette }

/** Access the active palette from any @Composable */
object AppTheme {
    val colors: OpenDroidColors
        @Composable
        @ReadOnlyComposable
        get() = LocalOpenDroidColors.current
}

// ── Dynamic Composable top-level aliases ────────────────────
// These allow all existing screens and composables to automatically
// adapt to Light / Dark theme dynamically without hardcoding DarkPalette!

val DarkBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.background

val DarkSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.surface

val CardBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.cardBackground

val BorderColor: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.borderColor

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.textPrimary

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.textSecondary

val AccentFuchsia: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.accentFuchsia

val AccentFuchsiaButton: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.accentFuchsiaButton

val AccentPurple: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.accentPurple

val AccentCyan: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.accentCyan

val AccentRed: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.accentRed

val AccentOrange: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.accentOrange
