package com.instasave.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Material 3 Expressive Shape Scale (UI_UX_DESIGN.md §2.3)
val InstaSaveShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),   // Buttons (16dp Expressive standard)
    extraLarge = RoundedCornerShape(24.dp)
)
