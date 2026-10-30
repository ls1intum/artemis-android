package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.flow.Flow

interface InitialReplyTextProvider {
    /** The owner clears the accepted draft with its persisted revision. */
    val handlesAcceptedSend: Boolean get() = false

    /** Null means the host/thread identity has not resolved; do not present an editable draft yet. */
    val editorSessionKey: kotlinx.coroutines.flow.Flow<Any?> get() = kotlinx.coroutines.flow.flowOf(this)

    val newMessageText: Flow<TextFieldValue>

    fun updateInitialReplyText(text: TextFieldValue)

    /** Captures the destination before an upload outlives the currently visible reply context. */
    fun captureAttachmentTarget(): ((String) -> Unit)? = null
}
