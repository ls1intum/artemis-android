package de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.render.state

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
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
            val currentMinIndex = lazyState.firstVisibleItemIndex
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

    companion object {
        val Saver: Saver<VerticalPdfReaderState, *> = listSaver(
            save = { state ->
                listOf(
                    state.pdfFile.url,
                    state.pdfFile.authToken,
                    state.pdfFile.filename,
                    state.isZoomEnabled,
                    state.lazyState.firstVisibleItemIndex,
                    state.lazyState.firstVisibleItemScrollOffset
                )
            },
            restore = { restoredList ->
                VerticalPdfReaderState(
                    PdfFile(restoredList[0] as String, restoredList[1] as String, restoredList[2] as String),
                    restoredList[3] as Boolean,
                ).apply {
                    lazyState = LazyListState(
                        firstVisibleItemIndex = restoredList[4] as Int,
                        firstVisibleItemScrollOffset = restoredList[5] as Int
                    )
                }
            }
        )
    }
}

@Composable
fun rememberVerticalPdfReaderState(
    pdfFile: PdfFile,
    isZoomEnabled: Boolean = true,
): VerticalPdfReaderState {
    return rememberSaveable(saver = VerticalPdfReaderState.Saver) {
        VerticalPdfReaderState(pdfFile, isZoomEnabled)
    }
}
