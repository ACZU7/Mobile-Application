package com.example.mobileapplication.ui.theme

import androidx.compose.ui.graphics.Color

// Colour tokens from docs/design/ui-spec.md (sampled from the approved mockups).
// Screens must not use these directly: read them through MaterialTheme.colorScheme
// or MaterialTheme.extendedColors so dark mode works.

// Light theme
val Teal = Color(0xFF2A565F)            // primary
val Coral = Color(0xFFD86347)           // accent – main call-to-action buttons, active stepper step
val SuccessGreen = Color(0xFF357544)    // success tick circles
val SurfaceWhite = Color(0xFFFFFFFF)    // screen background and cards
val SurfaceTint = Color(0xFFF5FBF8)     // highlighted cards, selected tiles
val ChipNeutral = Color(0xFFF2F3F5)     // unselected chips, upcoming stepper steps
val PlateYellow = Color(0xFFEBC038)     // UK number-plate input
val TextPrimary = Color(0xFF1C1C1C)
val TextSecondary = Color(0xFF6B6B6B)
val BorderLight = Color(0xFFE3E5E8)     // very light card border
val ErrorRed = Color(0xFFB3261E)

val StatusSubmittedBg = Color(0xFFDAE8FB)
val StatusSubmittedText = Color(0xFF1D4E89)
val StatusInProgressBg = Color(0xFFF6E0B6)
val StatusInProgressText = Color(0xFF7A4B00)
val StatusFixedBg = Color(0xFFDAEFE1)
val StatusFixedText = Color(0xFF1E5631)
val StatusNewBg = Color(0xFFF3D2C6)
val StatusNewText = Color(0xFF8A3A22)

// Dark theme: lighter versions of the brand colours so they stay readable on a dark background.
val TealDark = Color(0xFF8FCBD6)
val OnTealDark = Color(0xFF00363F)
val CoralDark = Color(0xFFFFB4A1)
val OnCoralDark = Color(0xFF5C1A0A)
val SuccessGreenDark = Color(0xFF8FD69E)
val SurfaceDark = Color(0xFF121416)
val SurfaceTintDark = Color(0xFF1C2A2C)
val ChipNeutralDark = Color(0xFF2B2E33)
val TextPrimaryDark = Color(0xFFE6E6E6)
val TextSecondaryDark = Color(0xFFA9ADB2)
val BorderDark = Color(0xFF3A3E44)
val ErrorRedDark = Color(0xFFF2B8B5)

val StatusSubmittedBgDark = Color(0xFF203A5C)
val StatusSubmittedTextDark = Color(0xFFCFE1FA)
val StatusInProgressBgDark = Color(0xFF4F3A12)
val StatusInProgressTextDark = Color(0xFFF6E0B6)
val StatusFixedBgDark = Color(0xFF1F3F2A)
val StatusFixedTextDark = Color(0xFFCDEBD6)
val StatusNewBgDark = Color(0xFF55291D)
val StatusNewTextDark = Color(0xFFF3D2C6)
