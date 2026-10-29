package com.example.mobileapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Background and text colour of one status chip. */
@Immutable
data class StatusColors(val container: Color, val content: Color)

/**
 * The spec's colour tokens that have no slot in the Material 3 colour scheme.
 *
 * Read them with `MaterialTheme.extendedColors` so the light or dark version is
 * picked automatically.
 */
@Immutable
data class ExtendedColors(
    val success: Color,
    val surfaceTint: Color,
    val chipNeutral: Color,
    val plateYellow: Color,
    /** Text on [plateYellow]. Dark in both themes, because the plate stays yellow. */
    val onPlateYellow: Color,
    val statusSubmitted: StatusColors,
    val statusInProgress: StatusColors,
    val statusFixed: StatusColors,
    val statusNew: StatusColors
)

val LightExtendedColors = ExtendedColors(
    success = SuccessGreen,
    surfaceTint = SurfaceTint,
    chipNeutral = ChipNeutral,
    plateYellow = PlateYellow,
    onPlateYellow = TextPrimary,
    statusSubmitted = StatusColors(StatusSubmittedBg, StatusSubmittedText),
    statusInProgress = StatusColors(StatusInProgressBg, StatusInProgressText),
    statusFixed = StatusColors(StatusFixedBg, StatusFixedText),
    statusNew = StatusColors(StatusNewBg, StatusNewText)
)

val DarkExtendedColors = ExtendedColors(
    success = SuccessGreenDark,
    surfaceTint = SurfaceTintDark,
    chipNeutral = ChipNeutralDark,
    plateYellow = PlateYellow,
    onPlateYellow = TextPrimary,
    statusSubmitted = StatusColors(StatusSubmittedBgDark, StatusSubmittedTextDark),
    statusInProgress = StatusColors(StatusInProgressBgDark, StatusInProgressTextDark),
    statusFixed = StatusColors(StatusFixedBgDark, StatusFixedTextDark),
    statusNew = StatusColors(StatusNewBgDark, StatusNewTextDark)
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

/** The app's extra colour tokens, for example `MaterialTheme.extendedColors.success`. */
val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current
