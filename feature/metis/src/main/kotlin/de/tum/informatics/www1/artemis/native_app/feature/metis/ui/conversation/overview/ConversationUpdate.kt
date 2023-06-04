package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.overview

import de.tum.informatics.www1.artemis.native_app.feature.metis.content.conversation.ConversationWebsocketDTO

sealed interface ConversationUpdate

data class ServerSentConversationUpdate(val update: ConversationWebsocketDTO) : ConversationUpdate

data class MarkMessagesRead(val conversationId: Long) : ConversationUpdate