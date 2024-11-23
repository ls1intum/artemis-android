package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

import android.util.Log
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisPostAction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisPostDTO

/**
 * Manages updates to the conversation over the web socket.
 */
class ConversationWebSocketUpdateUseCase(
    private val metisService: MetisService,
    private val metisStorageService: MetisStorageService
) {

    /**
     * Collect updates from the STOMP service and update the database accordingly.
     */
    suspend fun updatePosts(
        host: String,
        context: MetisContext,
        clientId: Long
    ) {
        metisService.subscribeToPostUpdates(
            courseId = context.courseId,
            clientId = clientId
        ).collect { websocketData ->
            if (websocketData is WebsocketProvider.WebsocketData.Message) {
                updateDatabaseWithDto(
                    dto = websocketData.message,
                    context = context,
                    host = host
                )
            }
        }
    }

    private suspend fun updateDatabaseWithDto(
        dto: MetisPostDTO,
        context: MetisContext,
        host: String
    ) {
        Log.d("WebSocket", "Received post update: ${dto.action} ${dto.post}")
        when (dto.action) {
            MetisPostAction.CREATE -> {
                metisStorageService.insertLiveCreatedPost(host, context, dto.post)
            }

            MetisPostAction.UPDATE -> {
                metisStorageService.updatePost(host, context, dto.post)
            }

            MetisPostAction.DELETE -> {
                metisStorageService.deletePosts(
                    host,
                    listOf(dto.post.id ?: return)
                )
            }

            MetisPostAction.NEW_MESSAGE -> {

            }
        }
    }
}