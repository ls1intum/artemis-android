package de.tum.informatics.www1.artemis.native_app.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

object Spacings {
    val ScreenHorizontalSpacing = 16.dp
    val ScreenHorizontalInnerSpacing = 8.dp
    val FabContentBottomPadding = 80.dp

    val EndOfScrollablePageSpacing = ScreenHorizontalSpacing

    object Post {
        val innerSpacing = 8.dp
    }
}


fun Modifier.endOfPagePadding() = padding(bottom = Spacings.EndOfScrollablePageSpacing)

fun Modifier.pagePadding() = padding(horizontal = Spacings.ScreenHorizontalSpacing).endOfPagePadding()