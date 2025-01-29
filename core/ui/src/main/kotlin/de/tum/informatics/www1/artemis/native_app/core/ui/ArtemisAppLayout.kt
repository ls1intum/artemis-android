package de.tum.informatics.www1.artemis.native_app.core.ui

import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable

sealed class ArtemisAppLayout {

    data object Phone : ArtemisAppLayout()
    data object Tablet : ArtemisAppLayout()
}

@Composable
fun getArtemisAppLayout(): ArtemisAppLayout {
    val widthSizeClass = getWindowSizeClass().widthSizeClass
    val heightSizeClass = getWindowSizeClass().heightSizeClass

    if (widthSizeClass > WindowWidthSizeClass.Compact &&
        heightSizeClass > WindowHeightSizeClass.Compact) {
        return ArtemisAppLayout.Tablet
    }

    return ArtemisAppLayout.Phone
}