package de.tum.informatics.www1.artemis.native_app.core.ui.pdf

import android.content.Context
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import de.tum.informatics.www1.artemis.native_app.core.ui.pdf.render.PdfRendering
import de.tum.informatics.www1.artemis.native_app.core.ui.pdf.render.state.PdfReaderState
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.Date

class PdfFile {

    private fun generateFileName(): String = "${Date().time}.pdf"

    fun load(
        coroutineScope: CoroutineScope,
        context: Context,
        state: PdfReaderState,
        width: Int,
        height: Int,
        authToken: String,
        portrait: Boolean
    ) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(state.uri.toString())
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
                        val file = File(context.cacheDir, generateFileName())
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