package de.tum.informatics.www1.artemis.native_app.core.ui.media

import android.content.Context
import android.graphics.pdf.PdfDocument
import androidx.test.core.app.ApplicationProvider
import com.sun.net.httpserver.HttpServer
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.*
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.render.state.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.net.InetSocketAddress
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk = [34], shadows = [FixturePdfRendererShadow::class, FixturePdfPageShadow::class])
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
class PdfCacheOwnershipTest {
    @Test fun repeatedAcquisitionWithinReaderSessionRetainsOneLocalCopy() = runBlocking {
        val directory = Files.createTempDirectory("pdf-cache-test").toFile()
        val cache = PdfDocumentCache(directory, maxEntries = 3)
        var downloads = 0
        try {
            repeat(12) { cache.acquire("same-document") { downloads++; it.writeText("PDF") }.close() }
            assertEquals(1, downloads)
            assertEquals(1, directory.listFiles()!!.count { it.extension == "pdf" })
        } finally { directory.deleteRecursively() }
    }

    @Test fun evictionBoundsInactiveFilesAndRetainsActiveShare() = runBlocking {
        val directory = Files.createTempDirectory("pdf-cache-share-test").toFile()
        val cache = PdfDocumentCache(directory, maxEntries = 3)
        try {
            val shared = cache.acquire("shared-document") { it.writeText("shared content") }
            cache.retainForShare(shared.file)
            shared.close()
            repeat(12) { index -> cache.acquire("other-$index") { it.writeText("PDF") }.close() }
            cache.prune()
            assertEquals("shared content", shared.file.readText())
            assertTrue(directory.listFiles()!!.count { it.extension == "pdf" } <= 3)
        } finally { directory.deleteRecursively() }
    }

    @Test(timeout = 60_000) fun actualPdfReaderModesReuseAuthenticatedLocalCopy() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val bytes = fixturePdf(3)
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val requests = AtomicInteger()
        server.createContext("/slides.pdf") { exchange ->
            requests.incrementAndGet()
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val origin = "http://127.0.0.1:${server.address.port}"
        val resource = PdfFile("$origin/slides.pdf", "test-token", "slides.pdf", origin, "alice-$origin-slides")
        try {
            val vertical = VerticalPdfReaderState(resource)
            resource.load(scope, context, vertical, 200, 1_200, true).join()
            assertNull(vertical.error)
            assertEquals(3, vertical.pdfPageCount)
            assertEquals("Partial sheet/multi-page viewport must label the first visible page", 1, vertical.currentPage)
            org.robolectric.util.ReflectionHelpers.setField(vertical, "lazyState", androidx.compose.foundation.lazy.LazyListState(firstVisibleItemIndex = 1))
            assertEquals(2, vertical.currentPage)
            val firstFile = vertical.file
            assertNotNull(firstFile)
            vertical.close()
            val horizontal = HorizontalPdfReaderState(resource)
            resource.load(scope, context, horizontal, 300, 200, false).join()
            assertNull(horizontal.error)
            assertEquals(firstFile, horizontal.file)
            assertEquals(1, requests.get())
            horizontal.close()
            val activity = org.robolectric.Robolectric.buildActivity(android.app.Activity::class.java).setup().get()
            resource.share(activity, firstFile!!)
            val chooser = org.robolectric.Shadows.shadowOf(activity).nextStartedActivity
            val send = chooser.getParcelableExtra(android.content.Intent.EXTRA_INTENT, android.content.Intent::class.java)!!
            val uri = send.getParcelableExtra(android.content.Intent.EXTRA_STREAM, android.net.Uri::class.java)!!
            val cache = PdfDocumentCache.shared(context.cacheDir)
            repeat(12) { index -> cache.acquire("pressure-$index") { it.writeText("PDF") }.close() }
            cache.prune()
            assertArrayEquals(bytes, context.contentResolver.openInputStream(uri)!!.use { it.readBytes() })
            activity.finish()
        } finally { scope.cancel(); server.stop(0) }
    }
    @Test(timeout = 60_000) fun reopeningStableUrlFetchesReplacementBytesAndPageCount() = freshOpen(false)
    @Test(timeout = 60_000) fun queryRevisionChangesBytesWithoutChangingStudyProgressIdentity() = freshOpen(true)

    private fun freshOpen(queryRevision: Boolean) = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val currentBytes = java.util.concurrent.atomic.AtomicReference(fixturePdf(1))
        val requests = AtomicInteger()
        server.createContext("/slides.pdf") { exchange ->
            requests.incrementAndGet()
            val body = currentBytes.get()
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.start()
        val origin = "http://127.0.0.1:${server.address.port}"
        val firstUrl = "$origin/slides.pdf" + if (queryRevision) "?version=1&token=old" else ""
        val nextUrl = "$origin/slides.pdf" + if (queryRevision) "?version=2&token=new" else ""
        val progress = mediaProgressKey("alice", origin, "lecture/1/attachment/2", firstUrl)
        assertEquals(progress, mediaProgressKey("alice", origin, "lecture/1/attachment/2", nextUrl))
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val firstResource = PdfFile(firstUrl, "token", "slides.pdf", origin, progress)
            val first = VerticalPdfReaderState(firstResource)
            firstResource.load(scope, context, first, 200, 300, true).join()
            assertNull(first.error)
            assertEquals(1, first.pdfPageCount)
            assertArrayEquals(currentBytes.get(), first.file!!.readBytes())
            first.close()
            currentBytes.set(fixturePdf(2))
            // Dismissal/reopen creates a new PdfFile even for an identical attachment ID/name/URL.
            val reopenedResource = PdfFile(nextUrl, "token", "slides.pdf", origin, progress)
            val reopened = VerticalPdfReaderState(reopenedResource)
            reopenedResource.load(scope, context, reopened, 200, 300, true).join()
            assertNull(reopened.error)
            assertEquals(2, reopened.pdfPageCount)
            assertArrayEquals(currentBytes.get(), reopened.file!!.readBytes())
            assertEquals(2, requests.get())
            reopened.close()
        } finally { scope.cancel(); server.stop(0) }
    }

    private fun fixturePdf(pageCount: Int): ByteArray {
        val buffer = java.io.ByteArrayOutputStream()
        val offsets = mutableListOf(0)
        buffer.write("%PDF-1.4\n".toByteArray())
        val kids = (3 until 3 + pageCount).joinToString(" ") { "$it 0 R" }
        val contentsId = 3 + pageCount
        val objects = listOf("<< /Type /Catalog /Pages 2 0 R >>", "<< /Type /Pages /Kids [$kids] /Count $pageCount >>") +
            List(pageCount) { "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 200 300] /Resources << >> /Contents $contentsId 0 R >>" } +
            "<< /Length 0 >>\nstream\nendstream"
        objects.forEachIndexed { index, body -> offsets += buffer.size(); buffer.write("${index + 1} 0 obj\n$body\nendobj\n".toByteArray()) }
        val xref = buffer.size()
        buffer.write("xref\n0 ${objects.size + 1}\n0000000000 65535 f \n".toByteArray())
        offsets.drop(1).forEach { buffer.write("%010d 00000 n \n".format(it).toByteArray()) }
        buffer.write("trailer\n<< /Size ${objects.size + 1} /Root 1 0 R >>\nstartxref\n$xref\n%%EOF\n".toByteArray())
        return buffer.toByteArray()
    }

}

/** Deterministic PDF geometry for the viewport/cache tests. Network, files, reader state and share
 * provider remain real; Robolectric has no working native PDF page-count implementation here. */
@org.robolectric.annotation.Implements(android.graphics.pdf.PdfRenderer::class)
class FixturePdfRendererShadow {
    private var pages = 0
    @org.robolectric.annotation.Implementation fun __constructor__(descriptor: android.os.ParcelFileDescriptor) {
        val input = android.os.ParcelFileDescriptor.AutoCloseInputStream(android.os.ParcelFileDescriptor.dup(descriptor.fileDescriptor))
        val source = input.use { it.readBytes().toString(Charsets.ISO_8859_1) }
        pages = Regex("/Count\\s+(\\d+)").find(source)!!.groupValues[1].toInt()
    }
    @org.robolectric.annotation.Implementation fun getPageCount(): Int = pages
    @org.robolectric.annotation.Implementation fun openPage(index: Int): android.graphics.pdf.PdfRenderer.Page =
        org.robolectric.shadow.api.Shadow.newInstanceOf(android.graphics.pdf.PdfRenderer.Page::class.java)
    @org.robolectric.annotation.Implementation fun close() {}
}
@org.robolectric.annotation.Implements(android.graphics.pdf.PdfRenderer.Page::class)
class FixturePdfPageShadow {
    @org.robolectric.annotation.Implementation fun getWidth(): Int = 200
    @org.robolectric.annotation.Implementation fun getHeight(): Int = 300
    @org.robolectric.annotation.Implementation fun close() {}
}
