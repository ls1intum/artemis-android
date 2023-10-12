package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network

import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisPostAction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisPostDTO
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.plus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
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

    /**
     * Collect updates from the STOMP service and update the database accordingly.
     */
    suspend fun updatePosts(
        host: String,
        context: MetisContext,
        coroutineContext: CoroutineContext
    ) {
        val flow = mutex.withLock {
            getMetisUpdateListenerFlow(context, coroutineContext)
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

                    MetisPostAction.NEW_MESSAGE -> {

                    }
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun getMetisUpdateListenerFlow(metisContext: MetisContext, context: CoroutineContext = EmptyCoroutineContext): Flow<WebsocketProvider.WebsocketData<MetisPostDTO>> =
        metisUpdateListenerMap
            .getOrPut(metisContext) {
                metisService.subscribeToPostUpdates(metisContext)
                    .shareIn(
                        GlobalScope + context,
                        SharingStarted.WhileSubscribed(stopTimeout = 10.seconds),
                        replay = 1
                    )
            }
}