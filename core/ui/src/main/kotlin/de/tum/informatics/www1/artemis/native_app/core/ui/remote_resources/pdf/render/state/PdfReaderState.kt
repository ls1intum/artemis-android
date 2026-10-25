package de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.render.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.PdfFile
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.render.PdfRendering
import java.io.File
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.PdfDocumentCache

abstract class PdfReaderState(
    val pdfFile: PdfFile,
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

    private var loadGeneration = 0L
    private var documentLease: PdfDocumentCache.Lease? = null

    @Synchronized internal fun beginLoad(): Long {
        mError = null
        return ++loadGeneration
    }

    @Synchronized internal fun isCurrentLoad(generation: Long): Boolean = generation == loadGeneration

    @Synchronized internal fun installDocument(generation: Long, lease: PdfDocumentCache.Lease, rendering: PdfRendering): Boolean {
        if (generation != loadGeneration) return false
        pdfRender?.close()
        documentLease?.close()
        documentLease = lease
        mFile = lease.file
        pdfRender = rendering
        return true
    }

    @Synchronized fun close() {
        loadGeneration++
        pdfRender?.close()
        pdfRender = null
        mFile = null
        documentLease?.close()
        documentLease = null
    }
}
