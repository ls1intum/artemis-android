package de.tum.informatics.www1.artemis.native_app.core.ui.media

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.sun.net.httpserver.HttpServer
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.*
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.render.state.VerticalPdfReaderState
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], shadows = [FixturePdfRendererShadow::class, FixturePdfPageShadow::class])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PdfOpenSnapshotTest {
    @Test(timeout = 60000)
    fun openPageRetainsExactBytesThroughRecreationAndReleasesThemAtPageClose() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val original = fixturePdf(3)
        val body = AtomicReference(original)
        val count = AtomicInteger()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { e ->
            count.incrementAndGet()
            val bytes = body.get()
            e.sendResponseHeaders(200, bytes.size.toLong())
            e.responseBody.use { it.write(bytes) }
        }
        server.start()
        val origin = "http://127.0.0.1:${server.address.port}"
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val file = PdfFile("$origin/slides.pdf", "", "slides.pdf", origin, "retained-page")
        val pageOwner = file.retainOpenDocument()
        try {
            val first = VerticalPdfReaderState(file)
            file.load(scope, context, first, 200, 900, true).join()
            assertEquals(3, first.pdfPageCount)
            first.close() // Old Activity's composition is gone; the page ViewModel is still alive.
            body.set(fixturePdf(2))
            val cache = PdfDocumentCache.shared(context.cacheDir)
            repeat(12) {
                cache
                    .acquire("recreation-pressure-$it") { target -> target.writeText("PDF") }
                    .close()
            }
            val recreated = VerticalPdfReaderState(file)
            file.load(scope, context, recreated, 300, 800, true).join()
            assertEquals(
                "Recreation must keep its validated PDF, never map page3 onto replacement bytes",
                3,
                recreated.pdfPageCount,
            )
            assertArrayEquals(original, recreated.file!!.readBytes())
            assertEquals(1, count.get())
            recreated.close()
            pageOwner.close()
            cache.prune()
            val reopened =
                VerticalPdfReaderState(
                    PdfFile("$origin/slides.pdf", "", "slides.pdf", origin, "retained-page")
                )
            reopened.pdfFile.load(scope, context, reopened, 200, 900, true).join()
            assertEquals(2, reopened.pdfPageCount)
            assertEquals(2, count.get())
            reopened.close()
        } finally {
            pageOwner.close()
            scope.cancel()
            server.stop(0)
        }
    }

    @Test(timeout = 60000)
    fun missingOpenedSnapshotNeverDownloadsReplacementWithTheOldPageMapping() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val count = AtomicInteger()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { e ->
            count.incrementAndGet()
            val bytes = fixturePdf(3)
            e.sendResponseHeaders(200, bytes.size.toLong())
            e.responseBody.use { it.write(bytes) }
        }
        server.start()
        val origin = "http://127.0.0.1:${server.address.port}"
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val file = PdfFile("$origin/slides.pdf", "", "slides.pdf", origin, "missing-snapshot")
        val owner = file.retainOpenDocument()
        try {
            val first = VerticalPdfReaderState(file)
            file.load(scope, context, first, 200, 900, true).join()
            assertTrue(file.hasRetainedDocument)
            first.file!!.delete()
            first.close()
            val restored = VerticalPdfReaderState(file)
            file.load(scope, context, restored, 200, 900, true).join()
            assertNotNull(restored.error)
            assertEquals(1, count.get())
            restored.close()
        } finally {
            owner.close()
            scope.cancel()
            server.stop(0)
        }
    }
}
