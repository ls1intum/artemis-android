package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network

import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketTopic
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisPostDTO
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ConversationWebsocketDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge

fun WebsocketProvider.subscribeToConversationUpdates(userId: Long, courseId: Long): Flow<ConversationWebsocketDto> {
    val topic = WebsocketTopic.getConversationMetaUpdateTopic(courseId, userId)

    return subscribeMessage(topic, ConversationWebsocketDto.serializer())
}


fun WebsocketProvider.subscribeToPostUpdates(
    courseId: Long,
    clientId: Long
): Flow<MetisPostDTO> {
    val courseWideTopic = WebsocketTopic.getCourseWideConversationUpdateTopic(courseId)
    val normalTopic = WebsocketTopic.getNormalConversationUpdateTopic(clientId)

    val courseWideUpdates = subscribeMessage(courseWideTopic, MetisPostDTO.serializer())
    val normalUpdates = subscribeMessage(normalTopic, MetisPostDTO.serializer())

    return merge(courseWideUpdates, normalUpdates)
}