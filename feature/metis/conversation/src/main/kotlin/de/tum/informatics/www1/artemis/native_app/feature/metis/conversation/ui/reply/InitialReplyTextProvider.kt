package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.flow.Flow

interface InitialReplyTextProvider {

    val newMessageText: Flow<TextFieldValue>

    fun updateInitialReplyText(text: TextFieldValue)
}
