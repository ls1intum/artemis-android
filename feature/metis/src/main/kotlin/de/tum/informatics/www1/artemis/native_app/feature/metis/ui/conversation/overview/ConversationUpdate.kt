package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.overview

import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.content.dto.ConversationWebsocketDto

sealed interface ConversationUpdate

data class ServerSentConversationUpdate(val update: ConversationWebsocketDto) : ConversationUpdate

data class MarkMessagesRead(val conversationId: Long) : ConversationUpdate