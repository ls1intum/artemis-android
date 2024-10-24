package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation

import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.InitialReplyTextProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class TestInitialReplyTextProvider(
    override val newMessageText: Flow<TextFieldValue> = flowOf(
        TextFieldValue("")
    )
) : InitialReplyTextProvider {
    override fun updateInitialReplyText(text: TextFieldValue) = Unit
}
