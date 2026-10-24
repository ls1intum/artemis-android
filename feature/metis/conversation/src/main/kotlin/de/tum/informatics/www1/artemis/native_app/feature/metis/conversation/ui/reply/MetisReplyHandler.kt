package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.MetisModificationTask
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.MetisModificationTaskHandler
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import kotlinx.coroutines.CompletableDeferred


data class MetisReplyHandlerInputActions <T: IBasePost> (
    val create: () -> MetisModificationTask,
    val edit: (T, String) -> MetisModificationTask,
    val delete: (T) -> MetisModificationTask,
    val react: (T, emojiId: String, create: Boolean) -> MetisModificationTask,
    val save: ((T) -> MetisModificationTask)?,
    val pin: ((T) -> MetisModificationTask)?,
    val resolve: ((T) -> MetisModificationTask)?,
) {
    companion object {
        fun <T : IBasePost> empty() = MetisReplyHandlerInputActions<T>(
            create = { CompletableDeferred() },
            edit = { _, _ -> CompletableDeferred() },
            delete = { _ -> CompletableDeferred() },
            react = { _, _, _ -> CompletableDeferred() },
            save = null,
            pin = null,
            resolve = null
        )
    }
}

internal data class MetisReplyHandlerOutputActions <T: IBasePost> (
    val edit: (T) -> Unit,
    val delete: (T) -> Unit,
    val react: (T, emojiId: String, create: Boolean) -> Unit,
    val save: (T) -> Unit,
    val pin: (T) -> Unit,
    val resolve: (T) -> Unit,
)


/**
 * Holds the necessary data about the reply mode and tasks going on. Exposes methods to update the associated states over the content lambda.
 */
@Composable
internal fun <T : IBasePost> MetisReplyHandler(
    initialReplyTextProvider: InitialReplyTextProvider,
    actions: MetisReplyHandlerInputActions<T>,
    content: @Composable (
        replyMode: ReplyMode,
        actionsDelegate: MetisReplyHandlerOutputActions<T>,
        updateFailureStateDelegate: (MetisModificationFailure?) -> Unit
    ) -> Unit
) {
    var metisModificationTask: MetisModificationTask? by remember {
        mutableStateOf(null)
    }
    MetisModificationTaskHandler(
        metisModificationTask
    )

    var editingPost: T? by remember { mutableStateOf(null) }
    var editingSessionKey by remember { mutableStateOf(0L) }
    val replyMode = rememberReplyMode(
        initialReplyTextProvider = initialReplyTextProvider,
        editingPost = editingPost,
        editingSessionKey = editingSessionKey,
        onClearEditingPost = {
            editingSessionKey++
            editingPost = null
        },
        onCreatePost = actions.create,
        onEditPost = actions.edit
    )

    val outputActions = remember(actions) {
        MetisReplyHandlerOutputActions<T>(
            edit = { post ->
                editingSessionKey++
                editingPost = post
            },
            delete = { post -> metisModificationTask = actions.delete(post) },
            react = { post, emojiId, create ->
                metisModificationTask = actions.react(post, emojiId, create)
            },
            save = { post ->
                actions.save?.let {
                    metisModificationTask = it(post)
                } },
            pin = { post ->
                actions.pin?.let {
                    metisModificationTask = it(post)
                }
            },
            resolve = { post ->
                actions.resolve?.let {
                    metisModificationTask = it(post)
                }
            },
        )
    }

    content(
        replyMode,
        outputActions
    ) { metisModificationTask = CompletableDeferred(it) }
}


@Composable
private fun <T : IBasePost> rememberReplyMode(
    initialReplyTextProvider: InitialReplyTextProvider,
    editingPost: T?,
    editingSessionKey: Long,
    onClearEditingPost: () -> Unit,
    onCreatePost: () -> MetisModificationTask,
    onEditPost: (T, String) -> MetisModificationTask
): ReplyMode {
    val newMessageText = initialReplyTextProvider.newMessageText.collectAsState(initial = TextFieldValue())

    val editorSessionKey by initialReplyTextProvider.editorSessionKey.collectAsState(initial = initialReplyTextProvider)

    val replyModeNewMessage =
        remember(initialReplyTextProvider::updateInitialReplyText, onCreatePost, editorSessionKey) {
            ReplyMode.NewMessage(
                currentText = newMessageText,
                onUpdateTextUpstream = initialReplyTextProvider::updateInitialReplyText,
                onCreateNewMessageUpstream = onCreatePost,
                onCaptureAttachmentTarget = initialReplyTextProvider::captureAttachmentTarget,
                editorSessionKey = editorSessionKey,
                handlesAcceptedSend = initialReplyTextProvider.handlesAcceptedSend
            )
        }
    var editingPostJob: MetisModificationTask? by remember { mutableStateOf(null) }
    var submittedEditingPost: T? by remember { mutableStateOf(null) }
    var submittedEditingSessionKey: Long? by remember { mutableStateOf(null) }
    var submittedEditingMode: ReplyMode.EditMessage? by remember { mutableStateOf(null) }
    var submittedEditingRevision: Long? by remember { mutableStateOf(null) }
    val currentEditingSessionKey by rememberUpdatedState(editingSessionKey)
    val currentEditingPost by rememberUpdatedState(editingPost)
    val clearEditingPost by rememberUpdatedState(onClearEditingPost)

    val replyMode: ReplyMode by remember(editingPost, editingSessionKey, replyModeNewMessage) {
        derivedStateOf {
            if (editingPost != null) {
                lateinit var editingMode: ReplyMode.EditMessage
                editingMode = ReplyMode.EditMessage(
                    editingPost,
                    onEditMessage = {
                        val job = onEditPost(editingPost, it)
                        submittedEditingPost = editingPost
                        submittedEditingSessionKey = editingSessionKey
                        submittedEditingMode = editingMode
                        submittedEditingRevision = editingMode.revision
                        editingPostJob = job
                        job
                    },
                    onCancelEditMessage = onClearEditingPost
                )
                editingMode
            } else {
                replyModeNewMessage
            }
        }
    }

    val currentMode by rememberUpdatedState(replyMode)
    AwaitDeferredCompletion(job = editingPostJob) { failure ->
        val submitted = submittedEditingMode
        if (failure == null && currentEditingPost == submittedEditingPost &&
            currentEditingSessionKey == submittedEditingSessionKey && currentMode === submitted &&
            submitted?.revision == submittedEditingRevision
        ) clearEditingPost()
        editingPostJob = null
        submittedEditingPost = null
        submittedEditingSessionKey = null
        submittedEditingMode = null
        submittedEditingRevision = null
    }

    return replyMode
}
