package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.upload

import android.content.ContentResolver
import android.database.MatrixCursor
import android.net.Uri
import android.provider.OpenableColumns
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import io.mockk.every
import io.mockk.mockk
import java.io.ByteArrayInputStream
import java.io.IOException
import org.junit.Assert.*
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class UploadContentReaderTest {
    private val uri = Uri.parse("content://provider/uploads/report.pdf")
    private val resolver = mockk<ContentResolver>()
    private fun provider(stream: ByteArrayInputStream?, cursor: MatrixCursor? = null) {
        every { resolver.openInputStream(uri) } returns stream
        every { resolver.query(uri, null, null, null, null) } returns cursor
    }

    @Test fun `unknown available size cannot bypass upload limit or read the entire file`() {
        val stream = ReportedSizeStream(ByteArray(1000), 0)
        provider(stream)
        assertThrows(UploadContentTooLargeException::class.java) { UploadContentReader.read(resolver, uri, maximumBytes = 16) }
        assertTrue(stream.closed)
        assertTrue("Read ${stream.bytesRead} bytes", stream.bytesRead <= 17)
    }

    @Test fun `exact size limit accepts bytes even when available size is unknown`() {
        val bytes = ByteArray(16) { it.toByte() }
        provider(ReportedSizeStream(bytes, 0))
        assertArrayEquals(bytes, UploadContentReader.read(resolver, uri, maximumBytes = 16).bytes)
    }

    @Test fun `incorrect reported size does not reject a valid small upload`() {
        val bytes = byteArrayOf(1, 2, 3)
        provider(ReportedSizeStream(bytes, Int.MAX_VALUE))
        assertArrayEquals(bytes, UploadContentReader.read(resolver, uri, maximumBytes = 16).bytes)
    }

    @Test fun `missing display name column falls back to uri without crashing`() {
        val cursor = MatrixCursor(arrayOf("other")).apply { addRow(arrayOf("value")) }
        provider(ByteArrayInputStream(byteArrayOf(1)), cursor)
        assertEquals("report.pdf", UploadContentReader.read(resolver, uri).fileName)
        assertTrue(cursor.isClosed)
    }

    @Test fun `empty provider cursor falls back to uri`() {
        provider(ByteArrayInputStream(byteArrayOf(1)), MatrixCursor(arrayOf(OpenableColumns.DISPLAY_NAME)))
        assertEquals("report.pdf", UploadContentReader.read(resolver, uri).fileName)
    }

    @Test fun `missing content produces a failure rather than an upload`() {
        provider(null)
        assertThrows(IOException::class.java) { UploadContentReader.read(resolver, uri) }
    }

    private class ReportedSizeStream(bytes: ByteArray, private val reported: Int) : ByteArrayInputStream(bytes) {
        var closed = false
        var bytesRead = 0
        override fun available() = reported
        override fun read(bytes: ByteArray, offset: Int, length: Int): Int = super.read(bytes, offset, length).also {
            if (it > 0) bytesRead += it
        }
        override fun close() { closed = true; super.close() }
    }
}
