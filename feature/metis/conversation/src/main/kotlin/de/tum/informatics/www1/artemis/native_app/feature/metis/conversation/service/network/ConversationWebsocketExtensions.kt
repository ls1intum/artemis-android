package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network

import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.content.dto.ConversationWebsocketDto
import kotlinx.coroutines.flow.Flow

internal fun WebsocketProvider.subscribeToConversationUpdates(userId: Long, courseId: Long): Flow<ConversationWebsocketDto> {
    val topic = "/user/topic/metis/courses/$courseId/conversations/user/$userId"

    return subscribeMessage(topic, ConversationWebsocketDto.serializer())
}