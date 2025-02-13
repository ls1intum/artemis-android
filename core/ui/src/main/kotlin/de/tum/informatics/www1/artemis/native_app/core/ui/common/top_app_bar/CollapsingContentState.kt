package de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource

class CollapsingContentState(
    initialCollapsingHeight: Float,
    initialOffset: Float,
    var isInitiallyForcedCollapsed: Boolean = false,
    val isCollapsingEnabled: Boolean = true
) {
    var collapsingHeight by mutableFloatStateOf(initialCollapsingHeight)
    var offset by mutableFloatStateOf(initialOffset)
    var isCollapsed by mutableStateOf(isInitiallyForcedCollapsed)

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (offset > -collapsingHeight) {
                isCollapsed = available.y < 0
            }
            return calculateOffset(available.y)
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset =
            when {
                available.y <= 0 -> Offset.Zero
                offset == 0f -> Offset.Zero
                else -> calculateOffset(available.y)
            }
    }

    private fun calculateOffset(delta: Float): Offset {
        val oldOffset = offset
        val newOffset = (oldOffset + delta).coerceIn(-collapsingHeight, 0f)
        offset = newOffset
        return Offset(0f, newOffset - oldOffset)
    }
}