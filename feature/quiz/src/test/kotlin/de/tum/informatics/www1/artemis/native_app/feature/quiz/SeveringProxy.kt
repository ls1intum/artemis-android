package de.tum.informatics.www1.artemis.native_app.feature.quiz

import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

/**
 * A connection to the server that a test can cut, and bring back: it forwards everything to the server until
 * [sever], which closes every connection and refuses new ones, like a network that is gone, until [restore].
 *
 * That is the only way to see what the app does when its connection is really lost: the websocket, and every
 * request, go through the same wire.
 */
internal class SeveringProxy(private val targetHost: String, private val targetPort: Int) : AutoCloseable {

    private val connections = CopyOnWriteArrayList<Socket>()
    private val accepted = AtomicInteger()

    /**
     * How many connections have been made through the proxy, which shows that something really went through it
     */
    val acceptedConnections: Int get() = accepted.get()
    private var listener: ServerSocket = listen(port = 0)

    /**
     * The port of the proxy, which does not change when it is severed and restored
     */
    val port: Int = listener.localPort

    val url: String get() = "http://127.0.0.1:$port"

    fun sever() {
        listener.close()
        connections.forEach { runCatching { it.close() } }
        connections.clear()
    }

    fun restore() {
        listener = listen(port)
    }

    /**
     * Stops accepting connections, but leaves the ones that are made alone: the websocket of the app outlives the
     * test that made it, and an error from a connection that is cut then surfaces in the next test.
     */
    override fun close() = runCatching { listener.close() }.let { }

    private fun listen(port: Int): ServerSocket {
        val server = ServerSocket(port, 50, InetAddress.getByName("127.0.0.1")).apply { reuseAddress = true }

        thread(isDaemon = true, name = "severing-proxy-accept") {
            while (!server.isClosed) {
                val client = runCatching { server.accept() }.getOrNull() ?: break
                runCatching { forward(client) }.onFailure { runCatching { client.close() } }
            }
        }
        return server
    }

    private fun forward(client: Socket) {
        val upstream = Socket(targetHost, targetPort)
        accepted.incrementAndGet()
        connections += client
        connections += upstream

        pump(client, upstream)
        pump(upstream, client)
    }

    private fun pump(from: Socket, to: Socket) {
        thread(isDaemon = true, name = "severing-proxy-pump") {
            runCatching { from.getInputStream().copyTo(to.getOutputStream()) }
            runCatching { from.close() }
            runCatching { to.close() }
        }
    }
}
