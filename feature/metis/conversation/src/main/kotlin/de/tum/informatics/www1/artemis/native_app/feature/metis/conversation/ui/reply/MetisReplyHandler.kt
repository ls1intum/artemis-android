package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    val replyMode = rememberReplyMode(
        initialReplyTextProvider = initialReplyTextProvider,
        editingPost = editingPost,
        onClearEditingPost = { editingPost = null },
        onCreatePost = actions.create,
        onEditPost = actions.edit
    )

    val outputActions = remember(actions) {
        MetisReplyHandlerOutputActions<T>(
            edit = { post -> editingPost = post },
            delete = { post ->
                actions.pin?.let {
                    metisModificationTask = it(post)
                }
            },
            react = { post, emojiId, create ->
                metisModificationTask = actions.react(post, emojiId, create)
            },
            save = { post -> metisModificationTask = actions.delete(post) },

            pin = { post ->
                actions.save?.let {
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
    onClearEditingPost: () -> Unit,
    onCreatePost: () -> MetisModificationTask,
    onEditPost: (T, String) -> MetisModificationTask
): ReplyMode {
    val newMessageText = initialReplyTextProvider.newMessageText.collectAsState(initial = TextFieldValue())

    val replyModeNewMessage =
        remember(initialReplyTextProvider::updateInitialReplyText, onCreatePost) {
            ReplyMode.NewMessage(
                currentText = newMessageText,
                onUpdateTextUpstream = initialReplyTextProvider::updateInitialReplyText,
                onCreateNewMessageUpstream = onCreatePost
            )
        }
    var editingPostJob: MetisModificationTask? by remember() { mutableStateOf(null) }

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