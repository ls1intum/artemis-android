package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.upload

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import java.io.ByteArrayOutputStream
import java.io.IOException

internal data class UploadContent(val fileName: String, val bytes: ByteArray)
internal class UploadContentTooLargeException : IOException()

/** Blocking provider work; call on Dispatchers.IO. Never trust InputStream.available as a file size. */
internal object UploadContentReader {
    fun read(resolver: ContentResolver, uri: Uri, maximumBytes: Int = 5 * 1024 * 1024): UploadContent {
        require(maximumBytes >= 0)
        val providerName = runCatching {
            resolver.query(uri, null, null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
            }
        }.getOrNull()
        val name = providerName?.takeIf { it.isNotBlank() }
            ?: uri.lastPathSegment.orEmpty().substringAfterLast('/').ifBlank { "attachment" }
        val bytes = resolver.openInputStream(uri)?.use { input ->
            val output = ByteArrayOutputStream(minOf(maximumBytes, DEFAULT_BUFFER_SIZE))
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                // Read at most one byte past the limit, including providers with
                // unknown or misleading metadata. The stream always closes.
                val remaining = maximumBytes.toLong() - output.size()
                val count = input.read(buffer, 0, minOf(buffer.size.toLong(), remaining + 1).toInt())
                if (count < 0) break
                if (count == 0) {
                    val next = input.read()
                    if (next < 0) break
                    if (remaining == 0L) throw UploadContentTooLargeException()
                    output.write(next)
                } else {
                    if (count > remaining) throw UploadContentTooLargeException()
                    output.write(buffer, 0, count)
                }
            }
            output.toByteArray()
        } ?: throw IOException("Selected content is unavailable")
        return UploadContent(name, bytes)
    }
}
