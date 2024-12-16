package de.tum.informatics.www1.artemis.native_app.core.ui.material.colors

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object PostColors {
    object Actions {
        val delete: Color
            @Composable get() = Color(0xffdc3545)
    }

    object Roles {
        val tutor: Color
            @Composable get() = Color(0xFFFD7E14)
        val student: Color
            @Composable get() = Color(0xFF0C9EB6)
        val instructor: Color
            @Composable get() = Color(0xFFB60000)
    }

    object StatusBackground {
        val resolving: Color
            @Composable get() = Color(0xFF28A745).copy(alpha = 0.2f)
        val pinned: Color
            @Composable get() = Color(0xFFFFA500).copy(alpha = 0.25f)
    }

    val editedHintText: Color
        @Composable get() = Color.Gray

    val unsentMessageText: Color
        @Composable get() = Color.Gray
}