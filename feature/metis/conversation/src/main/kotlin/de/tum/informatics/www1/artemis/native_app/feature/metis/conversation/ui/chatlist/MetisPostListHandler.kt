package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.DisplayPostOrder
import kotlinx.coroutines.launch

/**
 * Handles scrolling down to new items if the list was scrolled down before the new items came it.
 *
 * Shows a floating action button if there are new posts
 */
@Composable
internal fun <T : Any> MetisPostListHandler(
    modifier: Modifier,
    state: LazyListState,
    itemCount: Int,
    getItem: (Int) -> T?,
    order: DisplayPostOrder,
    content: @Composable BoxScope.() -> Unit
) {
    val scope = rememberCoroutineScope()

    var isScrolledDown by remember { mutableStateOf(false) }
    var prevBottomItem: T? by remember { mutableStateOf(null) }

    var hasNewUnseenPost by remember { mutableStateOf(false) }

    val bottomItemIndex = remember(order, itemCount) {
        when (order) {
            DisplayPostOrder.REVERSED -> 0
            DisplayPostOrder.REGULAR -> itemCount - 1
        }
    }

    val canScrollDown = when (order) {
        DisplayPostOrder.REVERSED -> state.canScrollBackward
        DisplayPostOrder.REGULAR -> state.canScrollForward
    }

    LaunchedEffect(isScrolledDown, itemCount, getItem, canScrollDown, bottomItemIndex) {
        if (itemCount > 0) {
            val newBottomItem = getItem(bottomItemIndex)
            // Check if new bottom item exists
            if (newBottomItem != prevBottomItem && isScrolledDown) {
                // new bottom item exists and we were previously scrolled to the bottom. Scroll down!
                state.animateScrollToItem(bottomItemIndex)
            } else if (newBottomItem != prevBottomItem) {
                //  new bottom item exists but we are not on bottom so instead show user button.
                hasNewUnseenPost = true
            }

            isScrolledDown = !canScrollDown
            prevBottomItem = newBottomItem

            if (isScrolledDown) {
                // we are scrolled down now, so we must have seen all posts.
                hasNewUnseenPost = false
            }
        } else {
            hasNewUnseenPost = false
        }
    }

    // use scaffold as a way to conveniently place the fab.
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            if (hasNewUnseenPost) {
                FloatingActionButton(
                    onClick = { scope.launch { state.scrollToItem(bottomItemIndex) } }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            content()
        }
    }
}
