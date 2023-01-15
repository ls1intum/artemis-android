package de.tum.informatics.www1.artemis.native_app.core.websocket.impl

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.hildan.krossbow.websocket.WebSocketClient
import org.hildan.krossbow.websocket.WebSocketConnection
import org.hildan.krossbow.websocket.WebSocketFrame

/**
 * Websocket client wrapper that calls onClose when anything goes wrong with the connection.
 * E.g. when the WLAN disconnects or when a heartbeat is missed.
 */
class CloseCallbackWebsocketClient(
    private val baseClient: WebSocketClient,
    private val onClose: () -> Unit
) : WebSocketClient {

    override suspend fun connect(url: String): WebSocketConnection {
        return CloseCallbackWebsocketConnectionProxy(baseClient.connect(url), onClose)
    }

    private class CloseCallbackWebsocketConnectionProxy(
        private val currentConnection: WebSocketConnection,
        private val onClose: () -> Unit
    ) : WebSocketConnection by currentConnection {

        private val _frames: Channel<WebSocketFrame> = Channel()
        override val incomingFrames: Flow<WebSocketFrame> = _frames.receiveAsFlow()

        init {
            CoroutineScope(CoroutineName("close-callback-watcher")).launch {
                try {
                    currentConnection.incomingFrames.collect {
                        _frames.send(it)
                    }

                    _frames.close()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    onClose()
                    _frames.close(e)
                }
            }
        }

        override suspend fun close(code: Int, reason: String?) {
            onClose()
            _frames.close(RuntimeException(reason))
        }
    }
}