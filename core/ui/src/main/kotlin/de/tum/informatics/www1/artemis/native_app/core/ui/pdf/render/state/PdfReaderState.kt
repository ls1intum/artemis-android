package de.tum.informatics.www1.artemis.native_app.core.ui.pdf.render.state

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import de.tum.informatics.www1.artemis.native_app.core.ui.pdf.render.PdfRendering
import java.io.File

abstract class PdfReaderState(
    val uri: Uri,
    val authToken: String,
    isZoomEnabled: Boolean = false
) {
    internal var mError by mutableStateOf<Throwable?>(null)
    val error: Throwable?
        get() = mError

    private var mIsZoomEnable by mutableStateOf(isZoomEnabled)
    val isZoomEnabled: Boolean
        get() = mIsZoomEnable

    internal var mScale by mutableFloatStateOf(1f)
    val scale: Float
        get() = mScale

    internal var offset by mutableStateOf(Offset(0f, 0f))

    internal var mFile by mutableStateOf<File?>(null)
    val file: File?
        get() = mFile

    internal var pdfRender by mutableStateOf<PdfRendering?>(null)

    internal var mLoadPercent by mutableIntStateOf(0)
    val loadPercent: Int
        get() = mLoadPercent

    val pdfPageCount: Int
        get() = pdfRender?.pageCount ?: 0

    open var currentPage by mutableIntStateOf(1)

    val isLoaded
        get() = mFile != null

    open var isScrolling by mutableStateOf(false)

    fun close() {
        pdfRender?.close()
        pdfRender = null
    }
}