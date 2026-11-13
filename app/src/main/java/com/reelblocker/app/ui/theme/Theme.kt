package com.reelblocker.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// =============================================================================
// ZEN BAUHAUS COLOR PALETTE (Terracotta, Saffron, and Warm Ivory Linen)
// =============================================================================

// Light Palette (Warm Art Canvas & Minimalist Balance)
val ZenIvory = Color(0xFFF9F6F0)              // Warm ivory linen art background
val ZenSurface = Color(0xFFFFFFFF)            // Elevated card surface
val ZenSurfaceVariant = Color(0xFFF2ECE1)     // Soft warm cream container
val ZenTerracotta = Color(0xFFD4573B)         // Primary: Burnt Terracotta Sphere
val ZenTerracottaContainer = Color(0xFFFDEEE9)// Delicate terracotta wash
val ZenSaffron = Color(0xFFE5A93C)            // Secondary: Warm Saffron Crescent Bowl
val ZenSaffronContainer = Color(0xFFFEF6E7)   // Delicate saffron wash
val ZenSage = Color(0xFF2D6A4F)               // Tertiary: Grounded Sage Green
val ZenSageContainer = Color(0xFFE8F5EE)       // Soft sage wash
val ZenEspresso = Color(0xFF1C1917)           // OnBackground: Deep warm charcoal / espresso
val ZenStone = Color(0xFF78716C)              // OnSurfaceVariant: Warm stone gray
val ZenOutline = Color(0xFFE6E0D4)            // Subtle warm outline border
val ZenOutlineVariant = Color(0xFFEFE9DE)

// Dark Palette (Warm Midnight Stone)
val ZenDarkBackground = Color(0xFF141210)
val ZenDarkSurface = Color(0xFF1D1A17)
val ZenDarkSurfaceVariant = Color(0xFF292522)
val ZenDarkTerracotta = Color(0xFFE86D51)
val ZenDarkTerracottaContainer = Color(0xFF4A1F16)
val ZenDarkSaffron = Color(0xFFF2BA52)
val ZenDarkSaffronContainer = Color(0xFF4D3610)
val ZenDarkSage = Color(0xFF52B788)
val ZenDarkOnBackground = Color(0xFFF5EFE6)
val ZenDarkOnSurfaceVariant = Color(0xFFA8A29E)
val ZenDarkOutline = Color(0xFF38332E)

private val LightColorScheme = lightColorScheme(
    primary = ZenTerracotta,
    onPrimary = Color.White,
    primaryContainer = ZenTerracottaContainer,
    onPrimaryContainer = Color(0xFF5C1C0D),
    secondary = ZenSaffron,
    onSecondary = Color.White,
    secondaryContainer = ZenSaffronContainer,
    onSecondaryContainer = Color(0xFF573A05),
    tertiary = ZenSage,
    onTertiary = Color.White,
    tertiaryContainer = ZenSageContainer,
    onTertiaryContainer = Color(0xFF0F3D2A),
    background = ZenIvory,
    onBackground = ZenEspresso,
    surface = ZenSurface,
    onSurface = ZenEspresso,
    surfaceVariant = ZenSurfaceVariant,
    onSurfaceVariant = ZenStone,
    outline = ZenOutline,
    outlineVariant = ZenOutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = ZenDarkTerracotta,
    onPrimary = Color(0xFF2E0C06),
    primaryContainer = ZenDarkTerracottaContainer,
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = ZenDarkSaffron,
    onSecondary = Color(0xFF3B2702),
    secondaryContainer = ZenDarkSaffronContainer,
    onSecondaryContainer = Color(0xFFFFE0A6),
    tertiary = ZenDarkSage,
    onTertiary = Color(0xFF003920),
    background = ZenDarkBackground,
    onBackground = ZenDarkOnBackground,
    surface = ZenDarkSurface,
    onSurface = ZenDarkOnBackground,
    surfaceVariant = ZenDarkSurfaceVariant,
    onSurfaceVariant = ZenDarkOnSurfaceVariant,
    outline = ZenDarkOutline
)

@Composable
fun ReelBlockerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled by default to retain custom Zen Bauhaus theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
