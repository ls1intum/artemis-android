package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost

data class PostActions(
    val requestEditPost: (() -> Unit)? = null,
    val requestDeletePost: (() -> Unit)? = null,
    val onClickReaction: (emojiId: String, create: Boolean) -> Unit = { _, _ -> },
    val onCopyText: () -> Unit = {},
    val onReplyInThread: (() -> Unit)? = null
) {
    val canPerformAnyAction: Boolean get() = requestDeletePost != null || requestEditPost != null
}

@Composable
fun rememberPostActions(
    post: IBasePost?,
    hasModerationRights: Boolean,
    clientId: Long,
    onRequestEdit: () -> Unit,
    onRequestDelete: () -> Unit,
    onClickReaction: (emojiId: String, create: Boolean) -> Unit,
    onReplyInThread: (() -> Unit)?
): PostActions {
    val clipboardManager = LocalClipboardManager.current

    return remember(
        post,
        hasModerationRights,
        clientId,
        onRequestEdit,
        onRequestDelete,
        onClickReaction,
        onReplyInThread,
        clipboardManager
    ) {
        if (post != null) {
            val hasEditPostRights = hasModerationRights || post.authorId == clientId

            PostActions(
                requestEditPost = if (hasEditPostRights) onRequestEdit else null,
                requestDeletePost = if (hasEditPostRights) onRequestDelete else null,
                onClickReaction = onClickReaction,
                onCopyText = {
                    clipboardManager.setText(AnnotatedString(post.content.orEmpty()))
                },
                onReplyInThread = onReplyInThread
            )
        } else {
            PostActions()
        }
    }
}
