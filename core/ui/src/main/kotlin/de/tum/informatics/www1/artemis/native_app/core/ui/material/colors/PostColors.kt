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
            @Composable get() = Color(0xff007bff)
        val student: Color
            @Composable get() = Color(0xff28a745)
        val instructor: Color
            @Composable get() = Color(0xff6c757d)
    }

    val editedHintText: Color
        @Composable get() = Color.Gray

    val unsentMessageText: Color
        @Composable get() = Color.Gray

    val pinnedMessageBackground: Color
        @Composable get() = Color(0xFFFFA500).copy(alpha = 0.25f)
}