package de.tum.informatics.www1.artemis.native_app.core.websocket.impl

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.hildan.krossbow.websocket.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val TAG = "WebSocketClient"

/**
 * Adapted from the original krossbow source.
 */

class CustomOkHttpWebSocketClient(
    private val client: OkHttpClient = OkHttpClient(),
    private val onError: () -> Unit
) : WebSocketClient {

    override suspend fun connect(url: String): WebSocketConnection {
        val request = Request.Builder().url(url).build()
        val channelListener = WebSocketListenerFlowAdapter()

        return suspendCancellableCoroutine { continuation ->
            val okHttpListener =
                KrossbowToOkHttpListenerAdapter(continuation, channelListener, onError)
            val ws = client.newWebSocket(request, okHttpListener)
            continuation.invokeOnCancellation {
                ws.cancel()
            }
        }
    }
}

private class KrossbowToOkHttpListenerAdapter(
    connectionContinuation: Continuation<WebSocketConnection>,
    private val channelListener: WebSocketListenerFlowAdapter,
    private val onError: () -> Unit
) : WebSocketListener() {
    private var connectionContinuation: Continuation<WebSocketConnection>? = connectionContinuation

    @Volatile
    private var isConnecting = true

    private inline fun completeConnection(resume: Continuation<WebSocketConnection>.() -> Unit) {
        val cont =
            connectionContinuation ?: error("OkHttp connection continuation already consumed")
        connectionContinuation = null // avoid leaking the continuation
        isConnecting = false
        cont.resume()
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        val krossbowConnection =
            OkHttpSocketToKrossbowConnectionAdapter(webSocket, channelListener.incomingFrames, onError)
        completeConnection { resume(krossbowConnection) }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        runBlocking { channelListener.onBinaryMessage(bytes.toByteArray()) }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        runBlocking { channelListener.onTextMessage(text) }
    }

    // overriding onClosing and not onClosed because we want to receive the Close frame from the server directly
    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        runBlocking { channelListener.onClose(code, reason) }
        Log.d(TAG, "Websocket close")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        if (isConnecting) {
            val exception = WebSocketConnectionException(
                url = webSocket.request().url.toString(),
                httpStatusCode = response?.code,
                cause = t,
            )
            completeConnection {
                resumeWithException(exception)
            }
        } else {
            channelListener.onError(t)
            onError()
        }
    }
}

private class OkHttpSocketToKrossbowConnectionAdapter(
    private val okSocket: WebSocket,
    override val incomingFrames: Flow<WebSocketFrame>,
    private val onMissingHeartbeat: () -> Unit
) : WebSocketConnection {

    override val url: String
        get() = okSocket.request().url.toString()

    override val canSend: Boolean
        get() = true // all send methods are just no-ops when the session is closed, so always OK

    override suspend fun sendText(frameText: String) {
        okSocket.send(frameText)
    }

    override suspend fun sendBinary(frameData: ByteArray) {
        okSocket.send(frameData.toByteString())
    }

    override suspend fun close(code: Int, reason: String?) {
        okSocket.close(code, reason)

        // 3002 is missing heartbeat exception, which we consider as an error and we have to reconnect.
        if (code == 3002) {
            Log.d(TAG, "Missing heartbeat")
            onMissingHeartbeat()
        }
    }
}
