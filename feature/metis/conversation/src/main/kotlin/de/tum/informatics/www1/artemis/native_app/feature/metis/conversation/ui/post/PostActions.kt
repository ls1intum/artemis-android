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
    val onResolvePost: (() -> Unit)? = null,
    val onPinPost: (() -> Unit)? = null,
    val onRequestRetrySend: () -> Unit = {}
) {
    val canPerformAnyAction: Boolean get() = requestDeletePost != null || requestEditPost != null
}

@Composable
fun rememberPostActions(
    post: IBasePost?,
    postActionFlags: PostActionFlags,
    clientId: Long,
    onRequestEdit: () -> Unit,
    onRequestDelete: () -> Unit,
    onClickReaction: (emojiId: String, create: Boolean) -> Unit,
    onReplyInThread: (() -> Unit)?,
    onResolvePost: (() -> Unit)?,
    onPinPost: (() -> Unit)?,
    onRequestRetrySend: () -> Unit
): PostActions {
    val clipboardManager = LocalClipboardManager.current

    return remember(
        post,
        postActionFlags,
        clientId,
        onRequestEdit,
        onRequestDelete,
        onClickReaction,
        onReplyInThread,
        onResolvePost,
        onPinPost,
        onRequestRetrySend,
        clipboardManager
    ) {
        if (post == null) {
            return@remember PostActions()
        }

        val doesPostExistOnServer = post.serverPostId != null
        val hasResolvePostRights =
            postActionFlags.isAtLeastTutorInCourse || post.authorId == clientId
        val hasPinPostRights = postActionFlags.isAbleToPin
        val isPostAuthor = post.authorId == clientId

        PostActions(
            requestEditPost = if (doesPostExistOnServer && isPostAuthor) onRequestEdit else null,
            requestDeletePost = if (isPostAuthor || postActionFlags.hasModerationRights) onRequestDelete else null,
            onClickReaction = if (doesPostExistOnServer) onClickReaction else null,
            onCopyText = {
                clipboardManager.setText(AnnotatedString(post.content.orEmpty()))
            },
            onReplyInThread = if (doesPostExistOnServer) onReplyInThread else null,
            onResolvePost = if (hasResolvePostRights) onResolvePost else null,
            onPinPost = if (hasPinPostRights) onPinPost else null,
            onRequestRetrySend = onRequestRetrySend,
        )
    }
}
