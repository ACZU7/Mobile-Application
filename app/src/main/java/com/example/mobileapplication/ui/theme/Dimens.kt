package com.example.mobileapplication.ui.theme

import androidx.compose.ui.unit.dp

/** Spacing and sizes from the UI spec, so every screen uses the same values. */
object Dimens {
    /** Padding around the edge of every screen. */
    val ScreenPadding = 16.dp

    /** Space between cards and tiles. */
    val CardSpacing = 12.dp

    /** Space inside small groups, for example an icon and its label. */
    val GroupSpacing = 8.dp

    /** Height of full-width buttons. */
    val ButtonHeight = 48.dp

    /** Width of outlines on buttons and unselected cards. */
    val BorderWidth = 1.dp

    /** Width of the teal outline on a selected tile. */
    val SelectedBorderWidth = 1.5.dp

    /** Content is limited to this width on tablets and in landscape so lines stay readable. */
    val MaxContentWidth = 600.dp
}
