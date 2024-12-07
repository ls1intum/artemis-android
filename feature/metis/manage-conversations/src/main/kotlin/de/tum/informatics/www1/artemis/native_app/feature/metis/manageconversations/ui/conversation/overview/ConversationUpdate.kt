package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisPostDTO
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ConversationWebsocketDto

sealed interface ConversationUpdate

data class MarkMessagesRead(val conversationId: Long) : ConversationUpdate

data class ServerSentConversationUpdate(val update: ConversationWebsocketDto) : ConversationUpdate

data class ServerSentPostUpdate(val update: MetisPostDTO) : ConversationUpdate

