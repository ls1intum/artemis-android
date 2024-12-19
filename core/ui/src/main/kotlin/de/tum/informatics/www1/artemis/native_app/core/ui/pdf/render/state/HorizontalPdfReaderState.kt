package de.tum.informatics.www1.artemis.native_app.core.ui.pdf.render.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import de.tum.informatics.www1.artemis.native_app.core.ui.pdf.PdfFile

class HorizontalPdfReaderState(
    pdfFile: PdfFile,
    isZoomEnabled: Boolean = false
) : PdfReaderState(pdfFile, isZoomEnabled) {

    companion object {
        val Saver: Saver<HorizontalPdfReaderState, *> = listSaver(
            save = { state ->
                listOf(
                    state.pdfFile.link,
                    state.pdfFile.authToken,
                    state.pdfFile.filename,
                    state.isZoomEnabled,
                    state.currentPage
                )
            },
            restore = { restoredList ->
                val pdfFile = PdfFile(restoredList[0] as String, restoredList[1] as String, restoredList[2] as String?)
                val isZoomEnabled = restoredList[2] as Boolean

                HorizontalPdfReaderState(
                    pdfFile = pdfFile,
                    isZoomEnabled = isZoomEnabled
                )
            }
        )
    }
}

@Composable
fun rememberHorizontalPdfReaderState(
    pdfFile: PdfFile,
    isZoomEnabled: Boolean = true,
): HorizontalPdfReaderState {
    return rememberSaveable(saver = HorizontalPdfReaderState.Saver) {
        HorizontalPdfReaderState(pdfFile, isZoomEnabled)
    }
}

