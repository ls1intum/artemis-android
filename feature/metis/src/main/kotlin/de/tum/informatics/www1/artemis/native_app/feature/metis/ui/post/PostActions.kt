package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post

import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.IBasePost

data class PostActions(
    val requestEditPost: (() -> Unit)? = null,
    val requestDeletePost: (() -> Unit)? = null,
    val onRequestReactWithEmoji: (emojiId: String) -> Unit = {}
) {
    val canPerformAnyAction: Boolean get() = requestDeletePost != null || requestEditPost != null
}

fun getPostActions(
    post: IBasePost,
    hasModerationRights: Boolean,
    clientId: Long,
    onRequestEdit: () -> Unit,
    onRequestDelete: () -> Unit,
    onRequestReactWithEmoji: (emojiId: String) -> Unit
): PostActions {
    val hasEditPostRights = hasModerationRights || post.authorId == clientId
    return PostActions(
        requestEditPost = if (hasEditPostRights) onRequestEdit else null,
        requestDeletePost = if (hasEditPostRights) onRequestDelete else null,
        onRequestReactWithEmoji = onRequestReactWithEmoji
    )
}
