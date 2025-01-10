package de.tum.informatics.www1.artemis.native_app.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

object Spacings {
    val ScreenHorizontalSpacing = 16.dp
    val EndOfScrollablePageSpacing = ScreenHorizontalSpacing

    object Post {
        val innerSpacing = 8.dp
    }


    /**
     * Calculate the padding for the bottom of the screen, considering the navigation bar height.
     * Remember to call `consumeWindowInsets(WindowInsets.navigationBars)` in the composable modifier.
     */
    @Composable
    fun calculateEndOfPagePaddingValues() = PaddingValues(
        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + EndOfScrollablePageSpacing
    )

    val BottomSheetContentPadding = PaddingValues(
        bottom = 40.dp,
        start = ScreenHorizontalSpacing,
        end = ScreenHorizontalSpacing
    )
}

fun Modifier.endOfPagePadding() = padding(bottom = Spacings.EndOfScrollablePageSpacing).navigationBarsPadding()

fun Modifier.pagePadding() = padding(horizontal = Spacings.ScreenHorizontalSpacing).endOfPagePadding()