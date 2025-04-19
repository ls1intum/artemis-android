package de.tum.informatics.www1.artemis.native_app.core.ui

import android.content.res.Configuration
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

sealed class ArtemisAppLayout {

    data object Phone : ArtemisAppLayout()
    data object Tablet : ArtemisAppLayout()
}

val ArtemisAppLayout.isTabletPortrait: Boolean
    @Composable get() = this == ArtemisAppLayout.Tablet && LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

val ArtemisAppLayout.isTabletLandscape: Boolean
    @Composable get() = this == ArtemisAppLayout.Tablet && LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

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