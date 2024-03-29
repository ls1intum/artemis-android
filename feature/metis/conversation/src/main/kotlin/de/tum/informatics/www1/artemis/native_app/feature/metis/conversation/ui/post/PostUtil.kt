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

@Composable
internal fun <T : IBasePost> shouldDisplayHeader(
    index: Int,
    post: T?,
    postCount: Int,
    order: DisplayPostOrder,
    getPost: (index: Int) -> T?
): Boolean {
    return remember(index, post, getPost, postCount) {
        val prefPost = when (order) {
            DisplayPostOrder.REVERSED -> if (index + 1 < postCount) getPost(index + 1) else null
            DisplayPostOrder.REGULAR -> if (index - 1 >= 0) getPost(index - 1) else null
        }

        val prefPostCreationDate = prefPost?.creationDate
        val postCreationDate = post?.creationDate
        if (prefPost != null && post != null && prefPost.authorId == post.authorId && prefPostCreationDate != null && postCreationDate != null) {
            postCreationDate - prefPostCreationDate > MaxDurationJoinMessages
        } else true
    }
}