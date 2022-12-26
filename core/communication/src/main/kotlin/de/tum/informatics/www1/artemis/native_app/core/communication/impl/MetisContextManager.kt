package de.tum.informatics.www1.artemis.native_app.core.communication.impl

import de.tum.informatics.www1.artemis.native_app.core.communication.MetisPostAction
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisPostDTO
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisService
import de.tum.informatics.www1.artemis.native_app.core.datastore.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketProvider
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

/**
 * Manages metis updates across the application.
 * Use this service to get access to the live metis updates and to apply changes sent by the server to the database.
 */
class MetisContextManager(
    private val metisService: MetisService,
    private val metisStorageService: MetisStorageService
) {

    /**
     * Make the manager threadsafe
     */
    private val mutex = Mutex()

    private val metisUpdateListenerMap: MutableMap<MetisContext, Flow<WebsocketProvider.WebsocketData<MetisPostDTO>>> =
        ConcurrentHashMap()

    private val updateListener: MutableMap<MetisContext, Flow<CurrentDataAction>> =
        ConcurrentHashMap()

    /**
     * Tells you what to do with the displayed data from the given metis context.
     */
    fun getContextDataActionFlow(metisContext: MetisContext): Flow<CurrentDataAction> {
        return flow {
            emitAll(mutex.withLock {
                val metisUpdateListener = getMetisUpdateListenerFlow(metisContext)

                updateListener.getOrPut(metisContext) {
                    metisUpdateListener.transform { websocketData ->
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
            })
        }
    }

    /**
     * Collect updates from the STOMP service and update the database accordingly.
     */
    suspend fun updatePosts(
        host: String,
        context: MetisContext
    ) {
        val flow = mutex.withLock {
            getMetisUpdateListenerFlow(context)
        }

        flow.collect { websocketData ->
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

                    MetisPostAction.READ_CONVERSATION -> {

                    }
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun getMetisUpdateListenerFlow(metisContext: MetisContext): Flow<WebsocketProvider.WebsocketData<MetisPostDTO>> =
        metisUpdateListenerMap
            .getOrPut(metisContext) {
                metisService.subscribeToPostUpdates(metisContext)
                    .shareIn(
                        GlobalScope,
                        SharingStarted.WhileSubscribed(stopTimeout = 10.seconds),
                        replay = 1
                    )
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

        /**
         * No action is required.
         */
        object Keep : CurrentDataAction
    }

}