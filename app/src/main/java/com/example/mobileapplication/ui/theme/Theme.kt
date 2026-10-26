package com.example.mobileapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

// The spec's tokens mapped onto Material 3: primary = teal, secondary = coral (accent),
// surface = white, onSurfaceVariant = text secondary, outlineVariant = light card border.
private val LightColorScheme = lightColorScheme(
    primary = Teal,
    onPrimary = SurfaceWhite,
    primaryContainer = SurfaceTint,
    onPrimaryContainer = Teal,
    secondary = Coral,
    onSecondary = SurfaceWhite,
    tertiary = SuccessGreen,
    onTertiary = SurfaceWhite,
    background = SurfaceWhite,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = ChipNeutral,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = SurfaceWhite,
    surfaceContainerLow = SurfaceWhite,
    surfaceContainer = SurfaceWhite,
    surfaceContainerHigh = SurfaceWhite,
    surfaceContainerHighest = ChipNeutral,
    outline = TextSecondary,
    outlineVariant = BorderLight,
    error = ErrorRed
)

private val DarkColorScheme = darkColorScheme(
    primary = TealDark,
    onPrimary = OnTealDark,
    primaryContainer = SurfaceTintDark,
    onPrimaryContainer = TealDark,
    secondary = CoralDark,
    onSecondary = OnCoralDark,
    tertiary = SuccessGreenDark,
    background = SurfaceDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = ChipNeutralDark,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainerLowest = SurfaceDark,
    surfaceContainerLow = SurfaceDark,
    surfaceContainer = SurfaceDark,
    surfaceContainerHigh = SurfaceDark,
    surfaceContainerHighest = ChipNeutralDark,
    outline = TextSecondaryDark,
    outlineVariant = BorderDark,
    error = ErrorRedDark
)

/**
 * The Bristol Connect theme.
 *
 * Dynamic (wallpaper) colours are deliberately not used, so the app always shows
 * the approved brand colours from the UI spec.
 */
@Composable
fun MobileApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalExtendedColors provides if (darkTheme) DarkExtendedColors else LightExtendedColors
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
