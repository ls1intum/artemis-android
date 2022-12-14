package de.tum.informatics.www1.artemis.native_app.core.communication.ui.view_post

internal sealed class ReplyState {
    data class CanCreate(val onCreateReply: (String) -> Unit) : ReplyState()
    data class IsSendingReply(val onCancelSendReply: () -> Unit) : ReplyState()
    object HasSentReply : ReplyState()
}