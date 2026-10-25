package de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.render.state

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.PdfFile

class VerticalPdfReaderState(
    pdfFile: PdfFile,
    isZoomEnabled: Boolean = false,
) : PdfReaderState(pdfFile, isZoomEnabled) {

    internal var lazyState: LazyListState = LazyListState()
        private set

    override var currentPage: Int = 0
        get() = if (pdfPageCount > 0) lazyState.firstVisibleItemIndex.coerceIn(0, pdfPageCount - 1) + 1 else 0

    override var isScrolling: Boolean = lazyState.isScrollInProgress

}

@Composable
fun rememberVerticalPdfReaderState(
    pdfFile: PdfFile,
    isZoomEnabled: Boolean = true,
): VerticalPdfReaderState {
    return remember(pdfFile) {
        VerticalPdfReaderState(pdfFile, isZoomEnabled)
    }
}
