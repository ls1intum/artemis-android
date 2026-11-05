package de.tum.informatics.www1.artemis.native_app.core.ui.media

import com.sun.net.httpserver.HttpServer
import okhttp3.Request
import org.junit.Assert.*
import org.junit.Test
import java.net.InetSocketAddress
import kotlinx.coroutines.launch

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.junit.experimental.categories.Category(de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest::class)
class MediaHttpClientTest {
    @Test fun cookieIsScopedAtEveryRedirectHop() {
        val source = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val other = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val origin = "http://127.0.0.1:${source.address.port}"
        var initialCookie: String? = null
        var redirectedCookie: String? = "not called"
        source.createContext("/redirect") { exchange ->
            initialCookie = exchange.requestHeaders.getFirst("Cookie")
            exchange.responseHeaders.add("Location", "http://127.0.0.1:${other.address.port}/video.mp4")
            exchange.sendResponseHeaders(302, -1); exchange.close()
        }
        other.createContext("/video.mp4") { exchange ->
            redirectedCookie = exchange.requestHeaders.getFirst("Cookie")
            exchange.sendResponseHeaders(200, 1)
            exchange.responseBody.use { it.write(byteArrayOf(1)) }
        }
        source.start(); other.start()
        try {
            mediaHttpClient(origin, "private-token").newCall(Request.Builder().url("$origin/redirect").build()).execute().use {
                assertTrue(it.isSuccessful)
            }
            assertEquals("jwt=private-token", initialCookie)
            assertNull(redirectedCookie)
        } finally { source.stop(0); other.stop(0) }
    }
    @Test fun failedMediaRequestCanBeRetried() = kotlinx.coroutines.runBlocking {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        var count = 0
        server.createContext("/media") { exchange ->
            val status = if (count++ == 0) 503 else 200
            exchange.sendResponseHeaders(status, 2)
            exchange.responseBody.use { it.write("ok".toByteArray()) }
        }
        server.start()
        val origin = "http://127.0.0.1:${server.address.port}"
        val client = mediaHttpClient(origin, "")
        val request = Request.Builder().url("$origin/media").build()
        try {
            val first = runCatching { withMediaResponse(client, request) { check(it.isSuccessful); it.body!!.string() } }
            assertTrue(first.isFailure)
            assertEquals("ok", withMediaResponse(client, request) { check(it.isSuccessful); it.body!!.string() })
        } finally { server.stop(0) }
    }

    @Test fun cancellingOwnerClosesInProgressReadAndSuppressesLateResult() = kotlinx.coroutines.runBlocking {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val release = java.util.concurrent.CountDownLatch(1)
        val reading = java.util.concurrent.CountDownLatch(1)
        val finished = java.util.concurrent.CountDownLatch(1)
        server.createContext("/media") { exchange ->
            try {
                exchange.sendResponseHeaders(200, 1024)
                exchange.responseBody.write(byteArrayOf(1)); exchange.responseBody.flush()
                release.await(30, java.util.concurrent.TimeUnit.SECONDS)
            } finally { exchange.close() }
        }
        server.start()
        val origin = "http://127.0.0.1:${server.address.port}"
        var delivered = false
        // Initialize OkHttp on the test thread before timing the asynchronous response read.
        val client = mediaHttpClient(origin, "")
        val job = launch(kotlinx.coroutines.Dispatchers.IO) {
            withMediaResponse(client, Request.Builder().url("$origin/media").build()) {
                reading.countDown()
                try { it.body!!.bytes() } finally { finished.countDown() }
            }
            delivered = true
        }
        try {
            assertTrue("The local fixture must start its response before cancellation", reading.await(15, java.util.concurrent.TimeUnit.SECONDS))
            job.cancel()
            job.join()
            assertTrue("The response read must stop with its owner", finished.await(5, java.util.concurrent.TimeUnit.SECONDS))
            assertFalse(delivered)
        } finally { job.cancel(); release.countDown(); server.stop(0) }
    }
    @Test fun credentialsAreNotReintroducedAfterCrossOriginRedirect() {
        val source = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val other = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val origin = "http://127.0.0.1:${source.address.port}"
        var finalCookie: String? = "not called"
        source.createContext("/start") { exchange ->
            exchange.responseHeaders.add("Location", "http://127.0.0.1:${other.address.port}/back")
            exchange.sendResponseHeaders(302, -1); exchange.close()
        }
        other.createContext("/back") { exchange ->
            exchange.responseHeaders.add("Location", "$origin/end")
            exchange.sendResponseHeaders(302, -1); exchange.close()
        }
        source.createContext("/end") { exchange ->
            finalCookie = exchange.requestHeaders.getFirst("Cookie")
            exchange.sendResponseHeaders(200, -1); exchange.close()
        }
        source.start(); other.start()
        try {
            mediaHttpClient(origin, "private-token").newCall(Request.Builder().url("$origin/start").build()).execute().close()
            assertNull(finalCookie)
        } finally { source.stop(0); other.stop(0) }
    }
}
