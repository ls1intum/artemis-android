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
    order: DisplayPostOrder,
    getPost: (index: Int) -> T?
): Boolean {
    return remember(index, post, getPost, postCount) {
        val prevPostIndex = when (order) {
            DisplayPostOrder.REVERSED -> index + 1
            DisplayPostOrder.REGULAR -> index - 1
        }

        val prevPost = when (order) {
            DisplayPostOrder.REVERSED -> if (index + 1 < postCount) getPost(prevPostIndex) else null
            DisplayPostOrder.REGULAR -> if (index - 1 >= 0) getPost(prevPostIndex) else null
        }

        val prefPostCreationDate = prevPost?.creationDate
        val postCreationDate = post?.creationDate
        if (prevPost != null && post != null && prevPost.authorId == post.authorId && prefPostCreationDate != null && postCreationDate != null) {
            postCreationDate - prefPostCreationDate > MaxDurationJoinMessages
        } else true
    }
}

@Composable
internal fun <T : IBasePost> determinePostItemViewJoinedType(
    index: Int,
    post: T?,
    postCount: Int,
    order: DisplayPostOrder,
    getPost: (index: Int) -> T?
): PostItemViewJoinedType {
    val isHeader = shouldDisplayHeader(index, post, postCount, order, getPost)

    val nextPostIndex = when (order) {
        DisplayPostOrder.REVERSED -> index - 1
        DisplayPostOrder.REGULAR -> index + 1
    }

    val nextPost = if (nextPostIndex in 0 until postCount) getPost(nextPostIndex) else null
    val isNextHeader = nextPost?.let {
        shouldDisplayHeader(nextPostIndex, it, postCount, order, getPost)
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
