package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

internal sealed class ReplyMode() {
    abstract val currentText: State<TextFieldValue>

    abstract fun onUpdate(new: TextFieldValue)

    data class NewMessage(
        override val currentText: State<TextFieldValue>,
        private val onUpdateTextUpstream: (TextFieldValue) -> Unit,
        private val onCreateNewMessageUpstream: () -> Deferred<de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure?>,
    ) : ReplyMode() {

        override fun onUpdate(new: TextFieldValue) {
            onUpdateTextUpstream(new)
        }

        fun onCreateNewMessage(): Deferred<de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure?> = onCreateNewMessageUpstream()
    }

    data class EditMessage(
        val post: IBasePost,
        private val onEditMessage: (String) -> Deferred<de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure?>,
        val onCancelEditMessage: () -> Unit
    ) : ReplyMode() {
        override val currentText = mutableStateOf(TextFieldValue(post.content ?: ""))

        override fun onUpdate(new: TextFieldValue) {
            currentText.value = new
        }

        fun onEditMessage(): Deferred<de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure?> = onEditMessage(currentText.value.text)
    }
}

@Composable
private fun <T : IBasePost> rememberReplyMode(
    initialReplyTextProvider: InitialReplyTextProvider,
    editingPost: T?,
    onClearEditingPost: () -> Unit,
    onCreatePost: () -> Deferred<de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure?>,
    onEditPost: (T, String) -> Deferred<de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure?>
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
    var editingPostJob: Deferred<de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure?>? by remember() { mutableStateOf(null) }

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
 * TODO https://github.com/ls1intum/artemis-android/issues/64:
 * MetisReplyHandler is not efficient to use, refactoring is needed.
 */
@Composable
internal fun <T : IBasePost> MetisReplyHandler(
    initialReplyTextProvider: InitialReplyTextProvider,
    onCreatePost: () -> Deferred<de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure?>,
    onEditPost: (T, String) -> Deferred<de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure?>,
    onResolvePost: ((T) -> Deferred<de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure?>)?,
    onPinPost: ((T) -> Deferred<de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure?>)?,
    onSavePost: ((T) -> Deferred<de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure?>)?,
    onDeletePost: (T) -> Deferred<de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure?>,
    onRequestReactWithEmoji: (T, emojiId: String, create: Boolean) -> Deferred<de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure?>,
    content: @Composable (
        replyMode: ReplyMode,
        onRequestEditPostDelegate: (T) -> Unit,
        onRequestResolvePostDelegate: (T) -> Unit,
        onRequestReactWithEmojiDelegate: (T, emojiId: String, create: Boolean) -> Unit,
        onDeletePostDelegate: (T) -> Unit,
        onPinPostDelegate: (T) -> Unit,
        onSavedPostDelegate: (T) -> Unit,
        updateFailureStateDelegate: (de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure?) -> Unit
    ) -> Unit
) {
    var metisModificationTask: Deferred<de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure?>? by remember {
        mutableStateOf(null)
    }
    de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.MetisModificationTaskHandler(
        metisModificationTask
    )


    var editingPost: T? by remember { mutableStateOf(null) }
    val replyMode = rememberReplyMode(
        initialReplyTextProvider = initialReplyTextProvider,
        editingPost = editingPost,
        onClearEditingPost = { editingPost = null },
        onCreatePost = onCreatePost,
        onEditPost = onEditPost
    )

    content(
        replyMode,
        { post -> editingPost = post },
        { post ->
            if (onResolvePost != null) {
                metisModificationTask = onResolvePost(post)
            }
        },
        { post, emojiId, create ->
            metisModificationTask = onRequestReactWithEmoji(post, emojiId, create)
        },
        { post -> metisModificationTask = onDeletePost(post) },
        { post ->
            if (onPinPost != null) {
                metisModificationTask = onPinPost(post)
            }
        },
        { post ->
            if (onSavePost != null) {
                metisModificationTask = onSavePost(post)
            }
        },
        { metisModificationTask = CompletableDeferred(it) }
    )
}
