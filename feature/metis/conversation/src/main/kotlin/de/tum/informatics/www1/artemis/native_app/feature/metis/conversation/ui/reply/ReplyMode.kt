package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.MetisModificationTask
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownStyleUtil

sealed class ReplyMode() {
    abstract val currentText: State<TextFieldValue>

    abstract fun onUpdate(new: TextFieldValue)

    data class NewMessage(
        override val currentText: State<TextFieldValue>,
        private val onUpdateTextUpstream: (TextFieldValue) -> Unit,
        val onCaptureAttachmentTarget: (() -> ((String) -> Unit)?)? = null,
        val editorSessionKey: Any? = currentText,
        val handlesAcceptedSend: Boolean = false,
        private val onCreateNewMessageUpstream: () -> MetisModificationTask,
    ) : ReplyMode() {

        constructor(
            currentText: State<TextFieldValue>,
            onUpdateTextUpstream: (TextFieldValue) -> Unit,
            onCreateNewMessageUpstream: () -> MetisModificationTask,
        ) : this(currentText, onUpdateTextUpstream, null, currentText, false, onCreateNewMessageUpstream)

        override fun onUpdate(new: TextFieldValue) {
            onUpdateTextUpstream(new)
        }

        fun onCreateNewMessage(): MetisModificationTask {
            val value = currentText.value
            val normalized = MarkdownStyleUtil.prepareForSend(value.text)
            if (normalized != value.text) onUpdateTextUpstream(value.copy(text = normalized, composition = null))
            return onCreateNewMessageUpstream()
        }
    }

    data class EditMessage(
        val post: IBasePost,
        private val onEditMessage: (String) -> MetisModificationTask,
        val onCancelEditMessage: () -> Unit
    ) : ReplyMode() {
        override val currentText = mutableStateOf(TextFieldValue(post.content ?: ""))
        var revision by mutableLongStateOf(0L)
            private set

        override fun onUpdate(new: TextFieldValue) {
            if (new.text != currentText.value.text) revision++
            currentText.value = new
        }

        fun onEditMessage(): MetisModificationTask = onEditMessage(MarkdownStyleUtil.prepareForSend(currentText.value.text))
    }
}


