package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IAnswerPost

data class PostActions(
    val requestEditPost: (() -> Unit)? = null,
    val requestDeletePost: (() -> Unit)? = null,
    val requestUndoDeletePost: (() -> Unit)? = null,
    val onClickReaction: ((emojiId: String, create: Boolean) -> Unit)? = null,
    val onCopyText: () -> Unit = {},
    val onReplyInThread: (() -> Unit)? = null,
    val onResolvePost: (() -> Unit)? = null,
    val onPinPost: (() -> Unit)? = null,
    val onForwardPost: (() -> Unit)? = null,
    val onSavePost: (() -> Unit)? = null,
    val onRequestRetrySend: () -> Unit = {}
) {
    val canPerformAnyAction: Boolean get() = requestDeletePost != null || requestEditPost != null
}

@Composable
fun rememberPostActions(
    chatListItem: ChatListItem.PostItem,
    postActionFlags: PostActionFlags,
    clientId: Long,
    onRequestEdit: () -> Unit,
    onRequestDelete: () -> Unit,
    onRequestUndoDelete: () -> Unit,
    onClickReaction: (emojiId: String, create: Boolean) -> Unit,
    onReplyInThread: (() -> Unit)?,
    onResolvePost: (() -> Unit)?,
    onPinPost: (() -> Unit)?,
    onForwardPost: (() -> Unit)?,
    onSavePost: (() -> Unit)?,
    onRequestRetrySend: () -> Unit
): PostActions {
    val clipboardManager = LocalClipboardManager.current

    return remember(
        chatListItem,
        postActionFlags,
        clientId,
        onRequestEdit,
        onRequestDelete,
        onRequestUndoDelete,
        onClickReaction,
        onReplyInThread,
        onResolvePost,
        onPinPost,
        onForwardPost,
        onSavePost,
        onRequestRetrySend,
        clipboardManager
    ) {
        val post = chatListItem.post

        val hasDeletedSourcePost = chatListItem is ChatListItem.PostItem.ForwardedMessage
                && chatListItem.forwardedPosts.isNotEmpty()
                && chatListItem.forwardedPosts[0] == null

        val doesPostExistOnServer = post.serverPostId != null
        val isPostAuthor = post.authorId == clientId
        val isParentPostAuthor = post is IAnswerPost && post.parentAuthorId == clientId
        val hasResolvePostRights =
            postActionFlags.isAtLeastTutorInCourse || isParentPostAuthor
        val hasPinPostRights = postActionFlags.isAbleToPin
        val hasDeletePostRights = isPostAuthor || postActionFlags.hasModerationRights
        val canForwardPost = (post.content.orEmpty().isEmpty() && hasDeletedSourcePost).not()

        PostActions(
            requestEditPost = if (doesPostExistOnServer && isPostAuthor) onRequestEdit else null,
            requestDeletePost = if (hasDeletePostRights) onRequestDelete else null,
            requestUndoDeletePost = if (hasDeletePostRights) onRequestUndoDelete else null,
            onClickReaction = if (doesPostExistOnServer) onClickReaction else null,
            onCopyText = {
                clipboardManager.setText(AnnotatedString(post.content.orEmpty()))
            },
            onReplyInThread = if (doesPostExistOnServer) onReplyInThread else null,
            onResolvePost = if (hasResolvePostRights) onResolvePost else null,
            onPinPost = if (hasPinPostRights) onPinPost else null,
            onForwardPost = if (canForwardPost) onForwardPost else null,
            onSavePost = if (doesPostExistOnServer) onSavePost else null,
            onRequestRetrySend = onRequestRetrySend,
        )
    }
}
