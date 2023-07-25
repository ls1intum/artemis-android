package de.tum.informatics.www1.artemis.native_app.feature.metis.ui

import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.conversation.ConversationWebsocketDTO
import kotlinx.coroutines.flow.Flow

internal fun WebsocketProvider.subscribeToConversationUpdates(userId: Long, courseId: Long): Flow<ConversationWebsocketDTO> {
    val topic = "/user/topic/metis/courses/$courseId/conversations/user/$userId"

    return subscribeMessage(topic, ConversationWebsocketDTO.serializer())
}