package de.tum.informatics.www1.artemis.native_app.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
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


    @Composable
    fun calculateEndOfPagePadding() = PaddingValues(
        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + EndOfScrollablePageSpacing
    )
}




@Composable
fun Modifier.endOfPagePadding() = padding(Spacings.calculateEndOfPagePadding())

@Composable
fun Modifier.pagePadding() = padding(horizontal = Spacings.ScreenHorizontalSpacing).endOfPagePadding()