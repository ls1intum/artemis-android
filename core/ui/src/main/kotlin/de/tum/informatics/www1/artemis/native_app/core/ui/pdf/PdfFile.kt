package de.tum.informatics.www1.artemis.native_app.core.ui.pdf

import android.app.DownloadManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.ContextCompat.getString
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import de.tum.informatics.www1.artemis.native_app.core.ui.R
import de.tum.informatics.www1.artemis.native_app.core.ui.pdf.render.PdfRendering
import de.tum.informatics.www1.artemis.native_app.core.ui.pdf.render.state.PdfReaderState
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.UnknownHostException
import java.util.Date


class PdfFile(
    val link: String,
    val authToken: String,
    val filename: String? = null
) {

    private fun generateFileName(): String = "${Date().time}.pdf"

    fun load(
        coroutineScope: CoroutineScope,
        context: Context,
        state: PdfReaderState,
        width: Int,
        height: Int,
        portrait: Boolean
    ) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(link)
            .header(HttpHeaders.Cookie, "jwt=$authToken")
            .build()

        runCatching {
            if (state.isLoaded) {
                coroutineScope.launch(Dispatchers.IO) {
                    runCatching {
                        val pFD =
                            ParcelFileDescriptor.open(
                                state.mFile,
                                ParcelFileDescriptor.MODE_READ_ONLY
                            )
                        state.pdfRender =
                            PdfRendering(pFD, width, height, portrait)
                    }.onFailure {
                        state.mError = it
                    }
                }
            } else {
                coroutineScope.launch(Dispatchers.IO) {
                    runCatching {

                        val bufferSize = 8192
                        var downloaded = 0
                        val file = File(context.cacheDir, filename ?: generateFileName())
                        val response = client.newCall(request).execute()
                        if (!response.isSuccessful) {
                            state.mError = Exception("Failed to download PDF: ${response.code}")
                            Log.e("PdfView", "Failed to download PDF: ${response.code}")
                        }

                        val byteStream = response.body?.byteStream()
                        byteStream.use { input ->
                            file.outputStream().use { output ->
                                val totalBytes = response.body?.contentLength()
                                var data = ByteArray(bufferSize)
                                var count = input?.read(data)
                                while (count != -1) {
                                    if (totalBytes != null) {
                                        if (totalBytes > 0) {
                                            downloaded += bufferSize
                                            state.mLoadPercent =
                                                (downloaded * (100 / totalBytes.toFloat())).toInt()
                                        }
                                    }
                                    if (count != null) {
                                        output.write(data, 0, count)
                                    }
                                    data = ByteArray(bufferSize)
                                    count = input?.read(data)
                                }
                            }
                        }
                        val pFD = ParcelFileDescriptor.open(
                            file,
                            ParcelFileDescriptor.MODE_READ_ONLY
                        )
                        state.pdfRender =
                            PdfRendering(pFD, width, height, portrait)
                        state.mFile = file
                    }.onFailure {
                        state.mError = it
                    }
                }
            }
        }.onFailure {
            state.mError = it
        }
    }

    fun downloadPdf(context: Context, isPdf: Boolean = true) {
        try {
            val downloadManager: DownloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadUri = link.toUri()

            downloadManager
                .enqueue(
                    DownloadManager.Request(downloadUri)
                        .addRequestHeader(HttpHeaders.Cookie, "jwt=$authToken")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setTitle(
                            filename ?: generateFileName()
                        )
                        .setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS,
                            downloadUri.lastPathSegment
                        )
                )

            Toast.makeText(
                context,
                getString(context, R.string.pdf_view_downloading_toast),
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {
            val errorMessage = when (e) {
                is UnknownHostException -> {
                    R.string.pdf_view_error_no_internet
                }
                else -> {
                    if (isPdf) R.string.pdf_view_error_downloading else R.string.pdf_view_error_downloading_non_pdf
                }
            }
            Toast.makeText(context, getString(context, errorMessage), Toast.LENGTH_LONG)
                .show()
            e.printStackTrace()
        }
    }

    fun sharePdf(context: Context, pdfFile: File) {
        val pdfUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
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