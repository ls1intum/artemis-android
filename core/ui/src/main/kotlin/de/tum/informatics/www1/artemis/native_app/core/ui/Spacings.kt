package de.tum.informatics.www1.artemis.native_app.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

object Spacings {
    val ScreenHorizontalSpacing = 16.dp
    val ScreenHorizontalInnerSpacing = 8.dp
    val FabContentBottomPadding = 80.dp

    object Post {
        val innerSpacing = 8.dp
    }

    val BottomSheetContentPadding = PaddingValues(
        bottom = 40.dp,
        start = ScreenHorizontalSpacing,
        end = ScreenHorizontalSpacing
    )
}