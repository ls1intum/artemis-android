package de.tum.informatics.www1.artemis.native_app.core.ui.pdf.render.state

import android.net.Uri
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.net.toUri

class VerticalPdfReaderState(
    uri: Uri,
    authToken: String,
    isZoomEnabled: Boolean = false,
) : PdfReaderState(uri, authToken, isZoomEnabled) {

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
            save = {
                val resourceUri = it.file?.toUri() ?: it.uri
                val authToken = it.authToken
                listOf(
                    resourceUri,
                    authToken,
                    it.isZoomEnabled,
                    it.lazyState.firstVisibleItemIndex,
                    it.lazyState.firstVisibleItemScrollOffset
                )
            },
            restore = {
                VerticalPdfReaderState(
                    it[0] as Uri,
                    it[1] as String,
                    it[2] as Boolean,
                ).apply {
                    lazyState = LazyListState(
                        firstVisibleItemIndex = it[3] as Int,
                        firstVisibleItemScrollOffset = it[4] as Int
                    )
                }
            }
        )
    }
}

@Composable
fun rememberVerticalPdfReaderState(
    uri: Uri,
    authToken: String,
    isZoomEnabled: Boolean = true,
): VerticalPdfReaderState {
    return rememberSaveable(saver = VerticalPdfReaderState.Saver) {
        VerticalPdfReaderState(uri, authToken, isZoomEnabled)
    }
}
