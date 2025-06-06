package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.OnTrueLaunchedEffect
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.LocalMarkdownTransformer
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.rememberPostArtemisMarkdownTransformer
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.service.EmojiService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.ui.ProvideEmojis
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.DisplayPostOrder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Handles scrolling down to new items if the list was scrolled down before the new items came in.
 *
 * Shows a floating action button if there are new posts
 */
@Composable
internal fun <T : Any> MetisPostListHandler(
    modifier: Modifier,
    serverUrl: String,
    courseId: Long,
    state: LazyListState,
    itemCount: Int,
    bottomItem: T?,
    order: DisplayPostOrder,
    emojiService: EmojiService,
    content: @Composable BoxScope.() -> Unit
) {
    val scope = rememberCoroutineScope()

    val hasNewUnseenPostState = remember { mutableStateOf(false) }
    val bottomItemIndex = remember(order, itemCount) {
        when (order) {
            DisplayPostOrder.REVERSED -> 0
            DisplayPostOrder.REGULAR -> itemCount - 1
        }
    }

    manageScrollToNewPost(
        state = state,
        itemCount = itemCount,
        bottomItem = bottomItem,
        order = order,
        bottomItemIndex = bottomItemIndex,
        hasNewUnseenPostState = hasNewUnseenPostState
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        val markdownTransformer = rememberPostArtemisMarkdownTransformer(serverUrl, courseId)

        ProvideEmojis(emojiService) {
            CompositionLocalProvider(LocalMarkdownTransformer provides markdownTransformer) {
                content()
            }
        }

        if (hasNewUnseenPostState.value) {
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Spacings.FabPaddingValues),
                onClick = { scope.launch { state.scrollToItem(bottomItemIndex) } }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = null
                )
            }
        }
    }
}

@SuppressLint("ComposableNaming")
@Composable
private fun <T: Any>manageScrollToNewPost(
    state: LazyListState,
    itemCount: Int,
    bottomItem: T?,
    order: DisplayPostOrder,
    bottomItemIndex: Int,
    hasNewUnseenPostState: MutableState<Boolean>
) {
    var prevBottomItem: T? by remember { mutableStateOf(null) }
    var requestScrollToBottom by remember { mutableStateOf(false) }
    var hasNewUnseenPost by hasNewUnseenPostState

    val canScrollDown = when (order) {
        DisplayPostOrder.REVERSED -> state.canScrollBackward
        DisplayPostOrder.REGULAR -> state.canScrollForward
    }

    LaunchedEffect(itemCount, bottomItem, canScrollDown, bottomItemIndex) {
        if (itemCount == 0) {
            hasNewUnseenPost = false
            return@LaunchedEffect
        }

        val isScrolledDown = !canScrollDown
        val doesNewPostExist = bottomItem != prevBottomItem

        if (doesNewPostExist) {
            if (isScrolledDown) {
                // new bottom item exists and we were previously scrolled to the bottom. Scroll down!
                requestScrollToBottom = true
            } else {
                // new bottom item exists but we are not on bottom so instead show user button.
                hasNewUnseenPost = true
            }
        }

        if (isScrolledDown) {
            // we are scrolled down now, so we must have seen all posts.
            hasNewUnseenPost = false
        }

        prevBottomItem = bottomItem
    }

    OnTrueLaunchedEffect(requestScrollToBottom) {
        // For the ChatList, the bottomPost and posts are not updated synchronously.
        // Therefore, we need to wait shortly before the new post is present in the posts
        // and we can actually scroll to it.
        delay(100.milliseconds)
        state.animateScrollToItem(bottomItemIndex)
        requestScrollToBottom = false
    }
}