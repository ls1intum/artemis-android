package de.tum.informatics.www1.artemis.native_app.core.ui.remote_images

import androidx.test.core.app.ApplicationProvider
import com.sun.net.httpserver.HttpServer
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextImpl
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.Assert.*
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Category(UnitTest::class)
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ReviewImageProviderTest {
    @org.junit.Before fun setMain() { kotlinx.coroutines.Dispatchers.setMain(kotlinx.coroutines.test.UnconfinedTestDispatcher()) }
    @org.junit.After fun resetMain() { kotlinx.coroutines.Dispatchers.resetMain() }

    private val context: android.content.Context = ApplicationProvider.getApplicationContext()
    private val image = javaClass.getResourceAsStream("/review/protected-background.png")!!.use { it.readBytes() }

    @Test(timeout = 30000) fun `same protected image is authorized anew after account and token change without shared cached pixels`() = runBlocking {
        val cookies = CopyOnWriteArrayList<String?>()
        val server = server { request ->
            cookies += request.requestHeaders.getFirst("Cookie")
            if (request.requestHeaders.getFirst("Cookie") == "jwt=account-a") respond(request, 200, image) else respond(request, 403, ByteArray(0))
        }
        var denied = 0
        val origin = "http://127.0.0.1:${server.address.port}"
        val a = ReviewImageProvider(context, ArtemisContextImpl.LoggedIn(origin, "account-a", "a")) { denied++ }
        val b = ReviewImageProvider(context, ArtemisContextImpl.LoggedIn(origin, "account-b", "b")) { denied++ }
        try {
            val loaded = a.loadArtemisImage(context, "review.png")
            assertTrue((loaded as? coil3.request.ErrorResult)?.throwable.toString(), loaded is coil3.request.SuccessResult)
            assertTrue(b.loadArtemisImage(context, "review.png") is coil3.request.ErrorResult)
            assertEquals(listOf("jwt=account-a", "jwt=account-b"), cookies)
            assertEquals(1, denied)
        } finally { a.close(); b.close(); server.stop(0) }
    }

    @Test(timeout = 30000) fun `foreign image gets no cookie and protected redirect never reaches foreign image server`() = runBlocking {
        val foreignCookies = CopyOnWriteArrayList<String?>()
        var foreignCalls = 0
        val foreign = server { request -> foreignCalls++; foreignCookies += request.requestHeaders.getFirst("Cookie"); respond(request, 200, image) }
        val foreignUrl = "http://127.0.0.1:${foreign.address.port}/image.png"
        val home = server { request -> request.responseHeaders.add("Location", foreignUrl); respond(request, 302, ByteArray(0)) }
        val provider = ReviewImageProvider(context, ArtemisContextImpl.LoggedIn("http://127.0.0.1:${home.address.port}", "private-token", "a")) {}
        try {
            val external = provider.markdownImageLoader.execute(coil.request.ImageRequest.Builder(context).data(foreignUrl).build())
            assertTrue(external is coil.request.SuccessResult)
            assertEquals(listOf<String?>(null), foreignCookies)
            assertTrue(provider.loadArtemisImage(context, "redirect.png") is coil3.request.ErrorResult)
            assertEquals(1, foreignCalls)
            assertTrue(provider.hasImageFailure.value)
        } finally { provider.close(); home.stop(0); foreign.stop(0) }
    }

    @Test(timeout = 30000) fun `Markdown relative protected image uses the captured cookie and its scoped loader`() = runBlocking {
        val cookies = CopyOnWriteArrayList<String?>()
        val server = server { request -> cookies += request.requestHeaders.getFirst("Cookie"); assertEquals("/api/core/files/diagram.png", request.requestURI.path); respond(request, 200, image) }
        val provider = ReviewImageProvider(context, ArtemisContextImpl.LoggedIn("http://127.0.0.1:${server.address.port}", "markdown-account", "student")) {}
        try {
            val result = provider.markdownImageLoader.execute(coil.request.ImageRequest.Builder(context).data("/api/core/files/diagram.png").build())
            assertTrue(result is coil.request.SuccessResult)
            assertEquals(listOf("jwt=markdown-account"), cookies)
        } finally { provider.close(); server.stop(0) }
    }

    private fun server(handler: (com.sun.net.httpserver.HttpExchange) -> Unit): HttpServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
        createContext("/") { request -> handler(request); request.close() }; start()
    }
    private fun respond(request: com.sun.net.httpserver.HttpExchange, status: Int, bytes: ByteArray) {
        request.responseHeaders.add("Content-Type", "image/png")
        request.sendResponseHeaders(status, bytes.size.toLong())
        request.responseBody.use { it.write(bytes) }
    }
}
