package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import kotlin.time.Duration.Companion.minutes

private val MaxDurationJoinMessages = 5.minutes

internal enum class DisplayPostOrder {
    REVERSED,
    REGULAR
}

internal enum class PostItemViewJoinedType {
    SINGLE,
    HEADER,
    JOINED,
    FOOTER,
    PARENT
}

@Composable
internal fun <T : IBasePost> shouldDisplayHeader(
    index: Int,
    post: T?,
    postCount: Int,
    unreadPostsCount: Int? = null,  // TODO: index can just be 0
    order: DisplayPostOrder,
    getPost: (index: Int) -> T?
): Boolean {
    return remember(index, post, getPost, postCount, unreadPostsCount) {
        val prevPostIndex = when (order) {
            DisplayPostOrder.REVERSED -> index + 1
            DisplayPostOrder.REGULAR -> index - 1
        }

        val prefPost = when (order) {
            DisplayPostOrder.REVERSED -> if (index + 1 < postCount) getPost(prevPostIndex) else null
            DisplayPostOrder.REGULAR -> if (index - 1 >= 0) getPost(prevPostIndex) else null
        }

        // TODO: refactor into two functions: isFirstUnreadPost() || isByDifferentAuthor() || isDeltaTimeBetweenPostsTooLarge()
        val firstUnreadPostIndex = when {
            unreadPostsCount == null -> null
            order == DisplayPostOrder.REVERSED -> unreadPostsCount - 1
            order == DisplayPostOrder.REGULAR -> postCount - unreadPostsCount
            else -> null // never reached
        }

        val isFirstUnreadPost = firstUnreadPostIndex != null && index == firstUnreadPostIndex
        if (isFirstUnreadPost) {
            return@remember true
        }

        val prefPostCreationDate = prefPost?.creationDate
        val postCreationDate = post?.creationDate
        if (prefPost != null && post != null && prefPost.authorId == post.authorId && prefPostCreationDate != null && postCreationDate != null) {
            postCreationDate - prefPostCreationDate > MaxDurationJoinMessages
        } else true
    }
}

@Composable
internal fun <T : IBasePost> determinePostItemViewJoinedType(
    index: Int,
    post: T?,
    postCount: Int,
    unreadPostsCount: Int? = null,
    order: DisplayPostOrder,
    getPost: (index: Int) -> T?
): PostItemViewJoinedType {
    val isHeader = shouldDisplayHeader(index, post, postCount, unreadPostsCount, order, getPost)

    val nextPostIndex = when (order) {
        DisplayPostOrder.REVERSED -> index - 1
        DisplayPostOrder.REGULAR -> index + 1
    }

    val nextPost = if (nextPostIndex in 0 until postCount) getPost(nextPostIndex) else null
    val isNextHeader = nextPost?.let {
        shouldDisplayHeader(nextPostIndex, it, postCount, unreadPostsCount, order, getPost)
    } ?: true

    return remember(isHeader, postCount, nextPost, isNextHeader) {
        if (isHeader) {
            if (nextPost == null || isNextHeader) {
                PostItemViewJoinedType.SINGLE
            } else {
                PostItemViewJoinedType.HEADER
            }
        } else {
            if (nextPost == null || isNextHeader) {
                PostItemViewJoinedType.FOOTER
            } else {
                PostItemViewJoinedType.JOINED
            }
        }
    }
}
