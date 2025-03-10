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
import androidx.compose.ui.unit.sp

object Spacings {
    val ScreenHorizontalSpacing = 16.dp
    val ScreenTopBarSpacing = ScreenHorizontalSpacing
    val EndOfScrollablePageSpacing = ScreenHorizontalSpacing
    val AppBarElevation = 8.dp

    /**
     * The spacing between the FAB and the end of the screen, according to the M3 guidelines:
     * https://m3.material.io/components/floating-action-button/specs#09dc2aff-7688-4d3b-9547-bd3ab606bd8b
     */
    val FabPaddingValues = PaddingValues(bottom = 16.dp, end = 16.dp)

    object Post {
        val innerSpacing = 8.dp

        val postHeadlineHeight = 36.dp
        val quoteBorderWidth = 5.dp
        val emojiHeight = 27.dp
        val emojiTextSize = 12.sp
        // The size of the emoji icon to open the emoji picker (measured in sp to support font scaling)
        val addEmojiIconSize = 18.sp
    }

    object Popup {
        val maxHeight = 270.dp

        val HintHorizontalPadding = 16.dp
        val ContentVerticalPadding = 8.dp

    }


    object CourseItem {
        val height = 250.dp
        val headerHeight = 70.dp
        val previewHeaderHeight = 40.dp
        val gridSpacing = 16.dp
    }

    object Faq {
        val categoryChipSpacing = 8.dp
    }

    /**
     * Calculate the padding for the bottom of the screen, considering the navigation bar height.
     * Remember to call `consumeWindowInsets(WindowInsets.navigationBars)` in the composable modifier.
     */
    @Composable
    fun calculateEndOfPagePaddingValues() = PaddingValues(
        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + EndOfScrollablePageSpacing
    )

    /**
     * Calculates the bottom padding according to calculateEndOfPagePaddingValues.
     * Adds the default ScreenTopBarSpacing to the top padding to add space between the top of the screen and the content.
     */
    @Composable
    fun calculateContentPaddingValues() = PaddingValues(
        top = ScreenTopBarSpacing,
        bottom = calculateEndOfPagePaddingValues().calculateBottomPadding()
    )

    val BottomSheetContentPadding = PaddingValues(
        bottom = 40.dp,
        start = ScreenHorizontalSpacing,
        end = ScreenHorizontalSpacing
    )

    object  UpdateScreen {
        val imageSize = 300.dp
        val large = 24.dp
        val medium = 16.dp
    }
}

fun Modifier.endOfPagePadding() = padding(bottom = Spacings.EndOfScrollablePageSpacing).navigationBarsPadding()

fun Modifier.pagePadding() = padding(horizontal = Spacings.ScreenHorizontalSpacing).endOfPagePadding()