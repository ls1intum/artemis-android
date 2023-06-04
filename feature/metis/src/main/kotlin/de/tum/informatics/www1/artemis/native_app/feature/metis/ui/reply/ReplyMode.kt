package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.reply

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.MetisModificationFailureDialog
import kotlinx.coroutines.Deferred

internal sealed class ReplyMode(initialText: String) {
    val currentText: MutableState<String> = mutableStateOf(initialText)

    fun onUpdateText(newText: String) {
        currentText.value = newText
    }

    data class NewMessage(
        private val onCreateNewMessage: (String) -> Deferred<MetisModificationFailure?>,
    ) : ReplyMode("") {
        fun onCreateNewMessage(): Deferred<MetisModificationFailure?> =
            onCreateNewMessage(currentText.value)
    }

    data class EditMessage(
        val post: IBasePost,
        private val onEditMessage: (String) -> Deferred<MetisModificationFailure?>,
        val onCancelEditMessage: () -> Unit
    ) : ReplyMode(post.content.orEmpty()) {
        fun onEditMessage(): Deferred<MetisModificationFailure?> = onEditMessage(currentText.value)
    }
}

@Composable
internal fun <T : IBasePost> rememberReplyMode(
    editingPost: T?,
    onClearEditingPost: () -> Unit,
    onCreatePost: (String) -> Deferred<MetisModificationFailure?>,
    onEditPost: (T, String) -> Deferred<MetisModificationFailure?>
): ReplyMode {
    val replyModeNewMessage = remember { ReplyMode.NewMessage(onCreatePost) }
    var editingPostJob: Deferred<MetisModificationFailure?>? by remember { mutableStateOf(null) }

    AwaitDeferredCompletion(job = editingPostJob) {
        onClearEditingPost()
        editingPostJob = null
    }

    val replyMode: ReplyMode by remember(editingPost, replyModeNewMessage) {
        derivedStateOf {
            if (editingPost != null) {
                ReplyMode.EditMessage(
                    editingPost,
                    onEditMessage = {
                        val job = onEditPost(editingPost, it)
                        editingPostJob = job
                        job
                    },
                    onCancelEditMessage = onClearEditingPost
                )
            } else {
                replyModeNewMessage
            }
        }
    }

    return replyMode
}

/**
 * Holds the necessary data about the reply mode and tasks going on. Exposes methods to update the associated states over the content lambda.
 */
@Composable
internal fun <T : IBasePost> MetisReplyHandler(
    onCreatePost: (String) -> Deferred<MetisModificationFailure?>,
    onEditPost: (T, String) -> Deferred<MetisModificationFailure?>,
    onDeletePost: (T) -> Deferred<MetisModificationFailure?>,
    onRequestReactWithEmoji: (T, emojiId: String, create: Boolean) -> Deferred<MetisModificationFailure?>,
    content: @Composable (
        replyMode: ReplyMode,
        onRequestEditPostDelegate: (T) -> Unit,
        onRequestReactWithEmojiDelegate: (T, emojiId: String, create: Boolean) -> Unit,
        onDeletePostDelegate: (T) -> Unit,
        updateFailureStateDelegate: (MetisModificationFailure?) -> Unit
    ) -> Unit
) {
    var metisFailure: MetisModificationFailure? by remember {
        mutableStateOf(null)
    }

    var metisModificationTask: Deferred<MetisModificationFailure?>? by remember {
        mutableStateOf(null)
    }

    AwaitDeferredCompletion(job = metisModificationTask) {
        metisFailure = it
    }

    var editingPost: T? by remember { mutableStateOf(null) }
    val replyMode = rememberReplyMode(
        editingPost = editingPost,
        onClearEditingPost = { editingPost = null },
        onCreatePost = onCreatePost,
        onEditPost = onEditPost
    )

    content(
        replyMode,
        { post -> editingPost = post },
        { post, emojiId, create ->
            metisModificationTask = onRequestReactWithEmoji(post, emojiId, create)
        },
        { post -> metisModificationTask = onDeletePost(post) },
        { metisFailure = it }
    )

    if (metisFailure != null) {
        MetisModificationFailureDialog(metisModificationFailure = metisFailure) {
            metisFailure = null
        }
    }
}