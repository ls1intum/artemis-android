package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network

import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketTopic
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ConversationWebsocketDto
import kotlinx.coroutines.flow.Flow

fun WebsocketProvider.subscribeToConversationUpdates(userId: Long, courseId: Long): Flow<ConversationWebsocketDto> {
    val topic = WebsocketTopic.getConversationMetaUpdateTopic(courseId, userId)

    return subscribeMessage(topic, ConversationWebsocketDto.serializer())
}