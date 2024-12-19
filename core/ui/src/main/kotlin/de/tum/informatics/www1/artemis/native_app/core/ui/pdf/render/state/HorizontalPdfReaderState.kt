package de.tum.informatics.www1.artemis.native_app.core.ui.pdf.render.state

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.net.toUri

class HorizontalPdfReaderState(
    uri: Uri,
    authToken: String,
    isZoomEnabled: Boolean = false
) : PdfReaderState(uri, authToken, isZoomEnabled) {

    companion object {
        val Saver: Saver<HorizontalPdfReaderState, *> = listSaver(
            save = { state ->
                listOf(
                    state.file?.toUri() ?: state.uri,
                    state.authToken,
                    state.isZoomEnabled,
                    state.currentPage
                )
            },
            restore = { restoredList ->
                val uri = restoredList[0] as Uri
                val authToken = restoredList[1] as String
                val isZoomEnabled = restoredList[2] as Boolean

                HorizontalPdfReaderState(
                    uri = uri,
                    authToken = authToken,
                    isZoomEnabled = isZoomEnabled
                )
            }
        )
    }
}

@Composable
fun rememberHorizontalPdfReaderState(
    uri: Uri,
    authToken: String,
    isZoomEnabled: Boolean = true,
): HorizontalPdfReaderState {
    return rememberSaveable(saver = HorizontalPdfReaderState.Saver) {
        HorizontalPdfReaderState(uri, authToken, isZoomEnabled)
    }
}

