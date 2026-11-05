package de.tum.informatics.www1.artemis.native_app.core.ui.media

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.io.IOException

/** A user-approved attachment download: authenticate each HTTP hop, then export local bytes. */
suspend fun downloadMediaAttachment(context: Context, serverUrl: String, token: String, url: String, name: String) {
    require(mediaHttpUri(url) != null) { "Invalid attachment URL" }
    val application = context.applicationContext
    val owner = currentCoroutineContext()
    val temporary = withContext(Dispatchers.IO) { File.createTempFile("attachment-", ".download", application.cacheDir) }
    try {
        withMediaResponse(mediaHttpClient(serverUrl, token), Request.Builder().url(url).build()) { response ->
            if (!response.isSuccessful) throw IOException("Attachment request failed (${response.code})")
            val body = response.body ?: throw IOException("Empty attachment response")
            body.byteStream().use { input -> temporary.outputStream().use { output ->
                val buffer = ByteArray(8192)
                while (true) {
                    owner.ensureActive()
                    val count = input.read(buffer)
                    if (count == -1) break
                    output.write(buffer, 0, count)
                }
            } }
        }
        owner.ensureActive()
        exportMediaFile(application, temporary, name, "application/octet-stream")
    } finally { withContext(kotlinx.coroutines.NonCancellable + Dispatchers.IO) { temporary.delete() } }
}

suspend fun exportMediaFile(context: Context, file: File, name: String, mimeType: String) = withContext(Dispatchers.IO) {
    val resolver = context.contentResolver
    val values = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, name.substringAfterLast('/').ifBlank { "attachment" })
        put(MediaStore.Downloads.MIME_TYPE, mimeType)
        put(MediaStore.Downloads.IS_PENDING, 1)
    }
    val destination = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        ?: throw IOException("Cannot create download")
    try {
        resolver.openOutputStream(destination)?.use { output -> file.inputStream().use { input -> input.copyTo(output) } }
            ?: throw IOException("Cannot write download")
        currentCoroutineContext().ensureActive()
        resolver.update(destination, ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) }, null, null)
    } catch (error: Exception) {
        resolver.delete(destination, null, null)
        throw error
    }
}
