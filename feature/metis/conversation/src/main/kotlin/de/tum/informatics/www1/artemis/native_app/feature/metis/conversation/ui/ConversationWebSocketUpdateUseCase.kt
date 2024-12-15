package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisCrudAction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisPostDTO
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.subscribeToPostUpdates

/**
 * Manages updates to the conversation over the web socket.
 */
class ConversationWebSocketUpdateUseCase(
    private val websocketProvider: WebsocketProvider,
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
        websocketProvider.subscribeToPostUpdates(
            courseId = context.courseId,
            clientId = clientId
        ).collect { postDto ->
            updateDatabaseWithDto(
                dto = postDto,
                context = context,
                host = host
            )
        }
    }

    private suspend fun updateDatabaseWithDto(
        dto: MetisPostDTO,
        context: MetisContext,
        host: String
    ) {
        when (dto.action) {
            MetisCrudAction.CREATE -> {
                metisStorageService.insertLiveCreatedPost(host, context, dto.post)
            }

            MetisCrudAction.UPDATE -> {
                metisStorageService.updatePost(host, context, dto.post)
            }

            MetisCrudAction.DELETE -> {
                metisStorageService.deletePosts(
                    host,
                    listOf(dto.post.id ?: return)
                )
            }
        }
    }
}