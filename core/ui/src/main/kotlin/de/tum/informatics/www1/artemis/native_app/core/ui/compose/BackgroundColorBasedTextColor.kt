package de.tum.informatics.www1.artemis.native_app.core.ui.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

object BackgroundColorBasedTextColor {

    fun of(backgroundColor: Color): Color {
        return if (backgroundColor.luminance() > 0.5f) {
            Color.Black
        } else {
            Color.White
        }
    }
}