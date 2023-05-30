package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post

internal sealed class ReplyState {
    data class CanCreate(val onCreateReply: () -> Unit) : ReplyState()
    data class IsSendingReply(val onCancelSendReply: () -> Unit) : ReplyState()
    object HasSentReply : ReplyState()
}