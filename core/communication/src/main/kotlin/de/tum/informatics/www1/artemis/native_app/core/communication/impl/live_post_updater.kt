package de.tum.informatics.www1.artemis.native_app.core.communication.impl

import de.tum.informatics.www1.artemis.native_app.core.communication.MetisPostAction
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisService
import de.tum.informatics.www1.artemis.native_app.core.datastore.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform

/**
 * Collect updates from the STOMP service and update the database accordingly.
 */
suspend fun updatePosts(
    host: String,
    metisService: MetisService,
    metisStorageService: MetisStorageService,
    context: MetisContext
): Flow<CurrentDataAction> {
    val updateFlow = metisService.subscribeToPostUpdates(context)
        .transform { websocketData ->
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
                            listOf(dto.post.id ?: return@transform)
                        )
                    }

                    MetisPostAction.READ_CONVERSATION -> {

                    }
                }
            }

            emit(websocketData)
        }

    return flow {
        updateFlow.collect { websocketData ->
            when (websocketData) {
                is WebsocketProvider.WebsocketData.Disconnect -> {
                    emit(CurrentDataAction.Outdated)
                }

                is WebsocketProvider.WebsocketData.Message -> {
                }

                is WebsocketProvider.WebsocketData.Subscribe -> {
                    emit(CurrentDataAction.Refresh)
                    emit(CurrentDataAction.Keep)
                }
            }
        }
    }
}

/**
 * Signal what to do with the currently cached data.
 */
sealed interface CurrentDataAction {
    /**
     * Keep the current data. But it is outdated.
     */
    object Outdated : CurrentDataAction

    /**
     * Clear current data and request new data.
     */
    object Refresh : CurrentDataAction

    object Keep : CurrentDataAction
}

