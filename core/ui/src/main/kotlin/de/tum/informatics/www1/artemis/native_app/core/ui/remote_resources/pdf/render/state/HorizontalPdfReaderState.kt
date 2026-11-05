package de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.render.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.PdfFile

class HorizontalPdfReaderState(
    pdfFile: PdfFile,
    isZoomEnabled: Boolean = false
) : PdfReaderState(pdfFile, isZoomEnabled) {

}

@Composable
fun rememberHorizontalPdfReaderState(
    pdfFile: PdfFile,
    isZoomEnabled: Boolean = true,
): HorizontalPdfReaderState {
    return remember(pdfFile) {
        HorizontalPdfReaderState(pdfFile, isZoomEnabled)
    }
}

