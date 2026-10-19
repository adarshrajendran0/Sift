package com.adarsh.sift.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),  // Nested text fields
    small = RoundedCornerShape(16.dp),       // List item cards
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(28.dp),       // Bottom sheets
    extraLarge = RoundedCornerShape(28.dp)
)
