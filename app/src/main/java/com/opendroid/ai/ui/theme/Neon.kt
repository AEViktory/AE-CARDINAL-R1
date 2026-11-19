package com.opendroid.ai.ui.theme

import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Signature neon colors for the retro-cyberpunk theme.
 * Use these for one-off highlights that should stay neon even when the
 * palette changes; for everything else prefer [AccentFuchsia] /
 * [AccentCyan] so the look follows the active theme.
 */
val NeonFuchsia: Color = Color(0xFFFF00E5)
val NeonCyan: Color = Color(0xFF00F0FF)

/**
 * A sharp neon border with a colored glow around the element.
 * The glow comes from a colored shadow; the border is drawn on top.
 * Pair with [RectangleShape] surfaces so the glow follows the cut corners.
 */
fun Modifier.neonBorder(
    color: Color,
    width: Dp = 1.dp,
    glow: Dp = 8.dp
): Modifier = this
    .shadow(
        elevation = glow,
        shape = RectangleShape,
        ambientColor = color,
        spotColor = color
    )
    .border(width = width, color = color, shape = RectangleShape)

/**
 * A colored neon glow around the element, without a visible border.
 * Useful for primary buttons, active chips and the chat input field.
 */
fun Modifier.neonGlow(
    color: Color,
    glow: Dp = 12.dp
): Modifier = this.shadow(
    elevation = glow,
    shape = RectangleShape,
    ambientColor = color,
    spotColor = color
)
