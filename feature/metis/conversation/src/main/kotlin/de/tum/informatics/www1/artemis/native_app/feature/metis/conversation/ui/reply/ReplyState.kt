package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds


internal sealed class ReplyState {
    data class CanCreate(val onCreateReply: () -> Unit) : ReplyState()
    data class IsSendingReply(val onCancelSendReply: () -> Unit) : ReplyState()
    object HasSentReply : ReplyState()
}



/**
 * Cycles through the reply state. When create reply is clicked, switches to sending reply.
 * If sending the reply was successful, shows has sent reply shortly.
 */
@Composable
internal fun rememberReplyState(
    replyMode: ReplyMode,
    updateFailureState: (de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure?) -> Unit
): ReplyState {
    var isCreatingReplyJob: Deferred<de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure?>? by remember { mutableStateOf(null) }
    var displaySendSuccess by remember { mutableStateOf(false) }

    AwaitDeferredCompletion(job = isCreatingReplyJob) { failure ->
        if (failure == null) {
            replyMode.onUpdate(TextFieldValue(text = ""))

            // Only show for edit.
            displaySendSuccess = replyMode is ReplyMode.EditMessage
        } else {
            updateFailureState(failure)
        }

        isCreatingReplyJob = null
    }

    LaunchedEffect(key1 = displaySendSuccess) {
        if (displaySendSuccess) {
            delay(1.seconds)

            displaySendSuccess = false
        }
    }

    return remember(isCreatingReplyJob, displaySendSuccess, replyMode) {
        when {
            isCreatingReplyJob != null -> ReplyState.IsSendingReply {
                isCreatingReplyJob?.cancel()
                isCreatingReplyJob = null
            }

            displaySendSuccess -> ReplyState.HasSentReply
            else -> ReplyState.CanCreate {
                isCreatingReplyJob = when (replyMode) {
                    is ReplyMode.EditMessage -> replyMode.onEditMessage()
                    is ReplyMode.NewMessage -> replyMode.onCreateNewMessage()
                }
            }
        }
    }
}