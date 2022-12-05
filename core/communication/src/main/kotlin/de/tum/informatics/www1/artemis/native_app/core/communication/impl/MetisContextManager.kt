package de.tum.informatics.www1.artemis.native_app.core.communication.impl

import de.tum.informatics.www1.artemis.native_app.core.communication.MetisService
import de.tum.informatics.www1.artemis.native_app.core.datastore.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

class MetisContextManager(
    private val metisService: MetisService,
    private val metisStorageService: MetisStorageService
) {

    /**
     * Make the manager threadsafe
     */
    private val mutex = Mutex()

    private val updateListener: MutableMap<MetisContext, Flow<CurrentDataAction>> =
        ConcurrentHashMap()

    /**
     * Performs the live updates in the database by subscribing to the websocket. Emits when the websocket is connected or disconnected
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun collectMetisUpdates(metisContext: MetisContext): Flow<CurrentDataAction> {
        return flow {
            emitAll(mutex.withLock {
                updateListener.getOrPut(metisContext) {
                    updatePosts("", metisService, metisStorageService, metisContext)
                        .shareIn(
                            GlobalScope,
                            SharingStarted.WhileSubscribed(stopTimeout = 10.seconds),
                            replay = 1
                        )
                }
            })
        }
    }

    suspend fun resetMetisContext(metisContext: MetisContext) {
        mutex.withLock {
            updateListener.remove(metisContext)
        }
    }
}