package com.opendroid.ai.ui.theme

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

/**
 * Applies edge-to-edge using the app's selected theme rather than the device's system theme.
 *
 * AndroidX uses the system UI mode to select the API 26-28 navigation-bar scrim by default.
 * OpenDroid supports an in-app theme toggle, so use the selected palette instead. On API 29+
 * [SystemBarStyle.auto] keeps both bars transparent, preserving edge-to-edge behavior.
 */
internal fun ComponentActivity.enableOpenDroidEdgeToEdge(isDarkTheme: Boolean) {
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.auto(
            lightScrim = Color.TRANSPARENT,
            darkScrim = Color.TRANSPARENT,
            detectDarkMode = { isDarkTheme }
        ),
        navigationBarStyle = SystemBarStyle.auto(
            lightScrim = LightPalette.background.toArgb(),
            darkScrim = DarkPalette.background.toArgb(),
            detectDarkMode = { isDarkTheme }
        )
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = DarkPalette.accentFuchsia,
    secondary = DarkPalette.accentCyan,
    tertiary = DarkPalette.accentPurple,
    background = DarkPalette.background,
    surface = DarkPalette.surface,
    onPrimary = DarkPalette.background,
    onSecondary = DarkPalette.background,
    onBackground = DarkPalette.textPrimary,
    onSurface = DarkPalette.textPrimary,
    error = DarkPalette.accentRed
)

private val LightColorScheme = lightColorScheme(
    primary = LightPalette.accentFuchsia,
    secondary = LightPalette.accentCyan,
    tertiary = LightPalette.accentPurple,
    background = LightPalette.background,
    surface = LightPalette.surface,
    onPrimary = LightPalette.background,
    onSecondary = LightPalette.background,
    onBackground = LightPalette.textPrimary,
    onSurface = LightPalette.textPrimary,
    error = LightPalette.accentRed
)

/**
 * Retro-cyberpunk shapes: sharp, cut corners everywhere (0 dp).
 * No rounded cards or buttons — the UI reads as a neon terminal.
 */
private val NeonShapes = Shapes(
    extraSmall = RoundedCornerShape(CornerSize(0.dp)),
    small = RoundedCornerShape(CornerSize(0.dp)),
    medium = RoundedCornerShape(CornerSize(0.dp)),
    large = RoundedCornerShape(CornerSize(0.dp)),
    extraLarge = RoundedCornerShape(CornerSize(0.dp))
)

@Composable
fun OpenDroidTheme(
    isDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val palette = if (isDarkTheme) DarkPalette else LightPalette
    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    val activity = view.context as? ComponentActivity
    if (!view.isInEditMode && activity != null) {
        SideEffect {
            activity.enableOpenDroidEdgeToEdge(isDarkTheme)
            val window = activity.window
            val insetsController = WindowCompat.getInsetsController(window, view)
            // Light status bar icons for dark theme, dark icons for light theme
            insetsController.isAppearanceLightStatusBars = !isDarkTheme
            insetsController.isAppearanceLightNavigationBars = !isDarkTheme
        }
    }

    CompositionLocalProvider(LocalOpenDroidColors provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = NeonShapes,
            content = content
        )
    }
}
