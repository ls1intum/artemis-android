package de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.ContextCompat.getString
import androidx.core.content.FileProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.R
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.BaseFile
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.render.PdfRendering
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.render.state.PdfReaderState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.CancellationException
import de.tum.informatics.www1.artemis.native_app.core.ui.media.mediaHttpClient
import de.tum.informatics.www1.artemis.native_app.core.ui.media.withMediaResponse
import java.io.IOException
import okhttp3.Request
import java.io.File

/**
 * Class representing a PDF file that can be downloaded and shared.
 * Used for lecture unit attachments and pdf attachments sent to the chat.
 *
 * @property url The URL of the PDF file.
 * @property authToken The authentication token used for downloading the file.
 * @property filename The name of the file.
 */
class PdfFile(
    override val url: String,
    override val authToken: String,
    override val filename: String,
    val serverUrl: String
): BaseFile(authToken, filename, url) {

    fun load(
        coroutineScope: CoroutineScope,
        context: Context,
        state: PdfReaderState,
        width: Int,
        height: Int,
        portrait: Boolean
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            var downloadedFile: File? = null
            try {
                val file = state.file ?: run {
                    val ownerContext = coroutineContext
                    withMediaResponse(mediaHttpClient(serverUrl, authToken), Request.Builder().url(url).build()) { response ->
                            if (!response.isSuccessful) throw IOException("PDF request failed (${response.code})")
                            val body = response.body ?: throw IOException("Empty PDF response")
                            val target = File.createTempFile("lecture-", ".pdf", context.cacheDir)
                            downloadedFile = target
                            body.byteStream().use { input ->
                                target.outputStream().use { output ->
                                    val buffer = ByteArray(8192)
                                    var total = 0L
                                    while (true) {
                                        ownerContext.ensureActive()
                                        val count = input.read(buffer)
                                        if (count == -1) break
                                        output.write(buffer, 0, count)
                                        total += count
                                        if (body.contentLength() > 0) state.mLoadPercent =
                                            (total * 100 / body.contentLength()).toInt().coerceIn(0, 100)
                                    }
                                }
                            }
                            target
                    }
                }
                coroutineContext.ensureActive()
                val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val rendering = try { PdfRendering(descriptor, width, height, portrait) }
                    catch (error: Throwable) { descriptor.close(); throw error }
                if (!isActive) { rendering.close(); return@launch }
                state.mFile = file
                state.pdfRender = rendering
            } catch (error: CancellationException) {
                downloadedFile?.delete()
                throw error
            } catch (error: Exception) {
                downloadedFile?.delete()
                if (isActive) state.mError = error
            }
        }
    }

    /** Export the already authenticated file; DownloadManager must not forward a session cookie. */
    fun download(context: Context, file: File) {
        CoroutineScope(Dispatchers.IO).launch {
            var destination: android.net.Uri? = null
            val resolver = context.contentResolver
            val message = try {
                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Downloads.DISPLAY_NAME, filename.substringAfterLast('/'))
                    put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(android.provider.MediaStore.Downloads.IS_PENDING, 1)
                }
                destination = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: throw IOException("Cannot create download")
                resolver.openOutputStream(destination)?.use { output -> file.inputStream().use { it.copyTo(output) } }
                    ?: throw IOException("Cannot write download")
                resolver.update(destination, android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Downloads.IS_PENDING, 0)
                }, null, null)
                R.string.file_downloading_toast
            } catch (_: Exception) {
                destination?.let { resolver.delete(it, null, null) }
                R.string.file_error_downloading
            }
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(context, getString(context, message), android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun share(context: Context, file: File) {
        val pdfUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            clipData = ClipData(
                "pdf",
                arrayOf("application/pdf"),
                ClipData.Item(pdfUri)
            )
            putExtra(Intent.EXTRA_STREAM, pdfUri) // to support sharing to older applications
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(
                shareIntent,
                getString(context, R.string.pdf_view_share_title)
            )
        )
    }
}

@Composable
internal fun PdfImage(
    bitmap: () -> ImageBitmap,
    contentDescription: String = "",
) {
    Image(
        bitmap = bitmap(),
        contentDescription = contentDescription,
        contentScale = ContentScale.FillWidth,
        modifier = Modifier.fillMaxWidth()
    )
}