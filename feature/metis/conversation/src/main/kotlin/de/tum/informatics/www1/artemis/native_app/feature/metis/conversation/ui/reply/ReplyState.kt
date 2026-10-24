package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.MetisModificationTask
import kotlinx.coroutines.delay
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownStyleUtil
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
    updateFailureState: (MetisModificationFailure?) -> Unit
): ReplyState {
    var isCreatingReplyJob: MetisModificationTask? by remember { mutableStateOf(null) }
    var displaySendSuccess by remember { mutableStateOf(false) }
    var submittedMode: ReplyMode? by remember { mutableStateOf(null) }
    var submittedBody: String? by remember { mutableStateOf(null) }
    var submittedRevision: Long? by remember { mutableStateOf(null) }
    val currentMode by rememberUpdatedState(replyMode)

    AwaitDeferredCompletion(job = isCreatingReplyJob) { failure ->
        if (failure == null) {
            val submitted = submittedMode
            if (submitted != null && currentMode === submitted &&
                submitted.currentText.value.text == submittedBody &&
                (submitted !is ReplyMode.EditMessage || submitted.revision == submittedRevision)
            ) {
                // The edit handler owns dismissal; clearing here races its revision guard.
                if (submitted is ReplyMode.NewMessage && !submitted.handlesAcceptedSend) {
                    submitted.onUpdate(TextFieldValue())
                }
                displaySendSuccess = submitted is ReplyMode.EditMessage
            }
        } else {
            updateFailureState(failure)
        }

        isCreatingReplyJob = null
        submittedMode = null
        submittedBody = null
        submittedRevision = null
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
                submittedMode = replyMode
                submittedBody = MarkdownStyleUtil.prepareForSend(replyMode.currentText.value.text)
                submittedRevision = (replyMode as? ReplyMode.EditMessage)?.revision
                isCreatingReplyJob = when (replyMode) {
                    is ReplyMode.EditMessage -> replyMode.onEditMessage()
                    is ReplyMode.NewMessage -> replyMode.onCreateNewMessage()
                }
            }
        }
    }
}