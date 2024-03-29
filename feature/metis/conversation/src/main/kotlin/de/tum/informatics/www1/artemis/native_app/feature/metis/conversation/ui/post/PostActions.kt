package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost

data class PostActions(
    val requestEditPost: (() -> Unit)? = null,
    val requestDeletePost: (() -> Unit)? = null,
    val onClickReaction: ((emojiId: String, create: Boolean) -> Unit)? = null,
    val onCopyText: () -> Unit = {},
    val onReplyInThread: (() -> Unit)? = null,
    val onRequestRetrySend: () -> Unit = {}
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
    onReplyInThread: (() -> Unit)?,
    onRequestRetrySend: () -> Unit
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
        onRequestRetrySend,
        clipboardManager
    ) {
        if (post != null) {
            val doesPostExistOnServer = post.serverPostId != null
            val hasEditPostRights = hasModerationRights || post.authorId == clientId

            PostActions(
                requestEditPost = if (doesPostExistOnServer && hasEditPostRights) onRequestEdit else null,
                requestDeletePost = if (hasEditPostRights) onRequestDelete else null,
                onClickReaction = if (doesPostExistOnServer) onClickReaction else null,
                onCopyText = {
                    clipboardManager.setText(AnnotatedString(post.content.orEmpty()))
                },
                onReplyInThread = if (doesPostExistOnServer) onReplyInThread else null,
                onRequestRetrySend = onRequestRetrySend
            )
        } else {
            PostActions()
        }
    }
}
