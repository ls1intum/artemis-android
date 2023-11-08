package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import kotlinx.coroutines.flow.Flow

interface InitialReplyTextProvider {

    val newMessageText: Flow<String>

    fun updateInitialReplyText(text: String)
}
