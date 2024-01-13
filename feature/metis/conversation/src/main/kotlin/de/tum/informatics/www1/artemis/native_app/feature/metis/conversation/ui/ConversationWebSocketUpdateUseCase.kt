package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisPostAction
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService

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
        context: MetisContext
    ) {
        metisService.subscribeToPostUpdates(context).collect { websocketData ->
            if (websocketData is WebsocketProvider.WebsocketData.Message) {
                val dto = websocketData.message
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
                            listOf(dto.post.id ?: return@collect)
                        )
                    }

                    MetisPostAction.NEW_MESSAGE -> {

                    }
                }
            }
        }
    }
}