package com.reelblocker.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// =============================================================================
// OFFICIAL BRAND COLOR PALETTE (From UI Specification Guide)
// =============================================================================

val ColorWarmOffWhite = Color(0xFFFBF9F1) // Primary Background
val ColorTerracotta   = Color(0xFFE37449) // Primary Brand / Accent (Action / High-Attention)
val ColorHoneyOchre   = Color(0xFFF3BE51) // Supporting / Focus (Accent / Categorization / Badges)
val ColorMatteCharcoal= Color(0xFF1E1E1E) // Text & Dark Elements (Contrast / Body text / Icons)
val ColorSoftSand     = Color(0xFFD8CDBC) // Secondary / Subtle Details (Structure / Borders)

// Supporting surface & tint derivations
val ColorCardSurface       = Color(0xFFFFFFFF) // Clean elevated white surface
val ColorTerracottaWash    = Color(0xFFFDF0EB) // Soft terracotta container
val ColorHoneyOchreWash    = Color(0xFFFEF8EC) // Soft honey ochre container
val ColorSoftSandWash      = Color(0xFFF5F1E6) // Soft subtle warm wash
val ColorDeepHoneyOchre    = Color(0xFFB45309) // High-contrast honey ochre for readable text/badges

private val LightColorScheme = lightColorScheme(
    primary = ColorTerracotta,
    onPrimary = Color.White,
    primaryContainer = ColorTerracottaWash,
    onPrimaryContainer = Color(0xFF6B260E),
    secondary = ColorHoneyOchre,
    onSecondary = ColorMatteCharcoal,
    secondaryContainer = ColorHoneyOchreWash,
    onSecondaryContainer = Color(0xFF614205),
    tertiary = ColorHoneyOchre,
    onTertiary = ColorMatteCharcoal,
    tertiaryContainer = ColorHoneyOchreWash,
    onTertiaryContainer = Color(0xFF614205),
    background = ColorWarmOffWhite,
    onBackground = ColorMatteCharcoal,
    surface = ColorCardSurface,
    onSurface = ColorMatteCharcoal,
    surfaceVariant = ColorWarmOffWhite,
    onSurfaceVariant = ColorMatteCharcoal.copy(alpha = 0.7f),
    outline = ColorSoftSand,
    outlineVariant = ColorSoftSand.copy(alpha = 0.5f)
)

@Composable
fun ReelBlockerTheme(
    darkTheme: Boolean = false, // Enforce the signature Warm Off-White brand palette
    dynamicColor: Boolean = false, // Always false to honor the exact custom color palette
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
