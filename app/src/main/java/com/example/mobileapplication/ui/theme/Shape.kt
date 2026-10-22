package com.example.mobileapplication.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Corner radii from the UI spec: text fields 10dp; cards, tiles and buttons 12dp.
// Chips are fully rounded (CircleShape) and are not part of this set.
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp), // outlined text fields
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(12.dp),     // cards, tiles, buttons
    large = RoundedCornerShape(16.dp)
)
