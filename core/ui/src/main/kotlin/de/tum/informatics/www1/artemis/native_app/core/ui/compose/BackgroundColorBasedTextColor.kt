package de.tum.informatics.www1.artemis.native_app.core.ui.compose

import androidx.compose.ui.graphics.Color

object BackgroundColorBasedTextColor {

    fun of(backgroundColor: Color): Color {
        return if (backgroundColor.brightnessW3() > 0.5f) {
            Color.Black
        } else {
            Color.White
        }
    }
}

// To be consistent with the Artemis webapp, we use the same calculation for brightness, which is
// based on the W3 guidelines.
// See https://github.com/ls1intum/Artemis/pull/10375/files#diff-43c9c251ad3fd69f3dcf4c7026aece29ac53a9318d0cf906ceef26544f7422f7
fun Color.brightnessW3(): Double {
    return 0.277 * red + 0.587 * green + 0.114 * blue
}