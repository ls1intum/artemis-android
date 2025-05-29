package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.MetisModificationTask
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost

sealed class ReplyMode() {
    abstract val currentText: State<TextFieldValue>

    abstract fun onUpdate(new: TextFieldValue)

    data class NewMessage(
        override val currentText: State<TextFieldValue>,
        private val onUpdateTextUpstream: (TextFieldValue) -> Unit,
        private val onCreateNewMessageUpstream: () -> MetisModificationTask,
    ) : ReplyMode() {

        override fun onUpdate(new: TextFieldValue) {
            onUpdateTextUpstream(new)
        }

        fun onCreateNewMessage(): MetisModificationTask = onCreateNewMessageUpstream()
    }

    data class EditMessage(
        val post: IBasePost,
        private val onEditMessage: (String) -> MetisModificationTask,
        val onCancelEditMessage: () -> Unit
    ) : ReplyMode() {
        override val currentText = mutableStateOf(TextFieldValue(post.content ?: ""))

        override fun onUpdate(new: TextFieldValue) {
            currentText.value = new
        }

        fun onEditMessage(): MetisModificationTask = onEditMessage(currentText.value.text)
    }
}


