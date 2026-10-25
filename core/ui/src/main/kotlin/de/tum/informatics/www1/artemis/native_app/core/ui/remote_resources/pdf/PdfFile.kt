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
    val serverUrl: String,
    documentScopeKey: String
): BaseFile(authToken, filename, url) {

    // Position identity deliberately ignores token/version queries. Bytes belong only to this open
    // reader: reuse between modes, but fetch again after dismissal/reopen even at a stable URL.
    private val readerCacheKey = "$documentScopeKey:${java.util.UUID.randomUUID()}"

    fun load(
        coroutineScope: CoroutineScope,
        context: Context,
        state: PdfReaderState,
        width: Int,
        height: Int,
        portrait: Boolean
    ): kotlinx.coroutines.Job {
        val generation = state.beginLoad()
        return coroutineScope.launch(Dispatchers.IO) {
            val cache = PdfDocumentCache.shared(context.applicationContext.cacheDir)
            var ownedLease: PdfDocumentCache.Lease? = null
            var rendering: PdfRendering? = null
            try {
                val owner = coroutineContext
                val lease = cache.acquire(readerCacheKey) { target ->
                    withMediaResponse(mediaHttpClient(serverUrl, authToken), Request.Builder().url(url).build()) { response ->
                        if (!response.isSuccessful) throw IOException("PDF request failed (${response.code})")
                        val body = response.body ?: throw IOException("Empty PDF response")
                        body.byteStream().use { input -> target.outputStream().use { output ->
                            val buffer = ByteArray(8192)
                            var total = 0L
                            while (true) {
                                owner.ensureActive()
                                val count = input.read(buffer)
                                if (count == -1) break
                                output.write(buffer, 0, count)
                                total += count
                                if (body.contentLength() > 0 && state.isCurrentLoad(generation)) {
                                    state.mLoadPercent = (total * 100 / body.contentLength()).toInt().coerceIn(0, 100)
                                }
                            }
                        } }
                    }
                }
                ownedLease = lease
                owner.ensureActive()
                val descriptor = ParcelFileDescriptor.open(lease.file, ParcelFileDescriptor.MODE_READ_ONLY)
                rendering = try { PdfRendering(descriptor, width, height, portrait) }
                    catch (error: Throwable) { descriptor.close(); throw error }
                owner.ensureActive()
                if (state.installDocument(generation, lease, rendering)) {
                    ownedLease = null
                    rendering = null
                }
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) {
                ownedLease?.let { cache.invalidate(it.file) }
                if (isActive && state.isCurrentLoad(generation)) state.mError = error
            } finally {
                rendering?.close()
                ownedLease?.close()
            }
        }
    }

    /** Export the already authenticated file; DownloadManager must not forward a session cookie. */
    fun download(context: Context, file: File) {
        val lease = PdfDocumentCache.shared(context.cacheDir).retain(file) ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val message = try {
                de.tum.informatics.www1.artemis.native_app.core.ui.media.exportMediaFile(context, file, filename, "application/pdf")
                R.string.file_downloading_toast
            } catch (_: Exception) { R.string.file_error_downloading }
            finally { lease.close() }
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(context, getString(context, message), android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun share(context: Context, file: File) {
        PdfDocumentCache.shared(context.cacheDir).retainForShare(file)
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