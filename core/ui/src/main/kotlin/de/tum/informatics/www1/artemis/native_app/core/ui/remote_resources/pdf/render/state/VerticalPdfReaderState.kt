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

    override var currentPage: Int = currentPage()
        get() = currentPage()

    override var isScrolling: Boolean = lazyState.isScrollInProgress

    private fun currentPage(): Int {
        return pdfRender?.let { pdfRender ->
            val currentMinIndex = lazyState.firstVisibleItemIndex.coerceIn(0, (pdfPageCount - 1).coerceAtLeast(0))
            var lastVisibleIndex = currentMinIndex
            var totalVisiblePortion =
                (pdfRender.pageLists[currentMinIndex].dimension.height * scale) - lazyState.firstVisibleItemScrollOffset
            for (i in currentMinIndex + 1 until pdfPageCount) {
                val newTotalVisiblePortion =
                    totalVisiblePortion + (pdfRender.pageLists[i].dimension.height * scale)
                if (newTotalVisiblePortion <= pdfRender.height) {
                    lastVisibleIndex = i
                    totalVisiblePortion = newTotalVisiblePortion
                } else {
                    break
                }
            }
            lastVisibleIndex + 1
        } ?: 0
    }

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
