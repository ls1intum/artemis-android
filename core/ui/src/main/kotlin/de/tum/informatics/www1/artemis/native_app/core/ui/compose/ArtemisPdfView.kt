package de.tum.informatics.www1.artemis.native_app.core.ui.compose

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.pdf.render.HorizontalPdfView
import de.tum.informatics.www1.artemis.native_app.core.ui.pdf.render.VerticalPdfView
import de.tum.informatics.www1.artemis.native_app.core.ui.pdf.render.state.HorizontalPdfReaderState
import de.tum.informatics.www1.artemis.native_app.core.ui.pdf.render.state.VerticalPdfReaderState
import de.tum.informatics.www1.artemis.native_app.core.ui.pdf.render.state.rememberHorizontalPdfReaderState
import de.tum.informatics.www1.artemis.native_app.core.ui.pdf.render.state.rememberVerticalPdfReaderState
import org.hildan.krossbow.stomp.ConnectionException

@Composable
fun ArtemisPdfView(
    modifier: Modifier,
    filename: String? = null,
    url: String,
    authToken: String,
) {
    val isVertical = remember { mutableStateOf(true) }

    val verticalPdfState = rememberVerticalPdfReaderState(
        uri = Uri.parse(url),
        isZoomEnabled = true,
        authToken = authToken
    )
    val horizontalPdfState = rememberHorizontalPdfReaderState(
        uri = Uri.parse(url),
        isZoomEnabled = true,
        authToken = authToken
    )

    val pdfState = if (isVertical.value) verticalPdfState else horizontalPdfState

    if (pdfState.mError != null) {
        when (pdfState.mError) {
            is ConnectionException -> {
                //TODO
            }
            else -> {
                //TODO
            }
        }
        Toast.makeText(
            LocalContext.current,
            "Error loading PDF: ${pdfState.mError?.message}",
            Toast.LENGTH_LONG
        ).show()
    }

    Box(
        modifier = modifier.padding(8.dp),
    ) {
        Column {
            pdfState.file?.let {
                Text(
                    text = filename ?: it.name,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (isVertical.value) {
                VerticalPdfView(
                    state = pdfState as VerticalPdfReaderState,
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.surface)
                        .fillMaxSize()
                )
            } else {
                HorizontalPdfView(
                    state = pdfState as HorizontalPdfReaderState,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.medium
                        )
                )
            }
        }

        FloatingActionButton(
            onClick = {
                isVertical.value = !isVertical.value
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (isVertical.value) Icons.AutoMirrored.Filled.RotateLeft else Icons.AutoMirrored.Filled.RotateRight,
                contentDescription = null
            )
        }

//        FloatingActionButton(
//            onClick = { /* Share logic */ },
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .zIndex(1f)
//                .padding(16.dp)
//        ) {
//            Icon(
//                imageVector = Icons.Default.Share,
//                contentDescription = null
//            )
//        }

        if (!pdfState.isLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { pdfState.loadPercent / 100f },
                    modifier = Modifier.height(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart).padding(top = 54.dp, end = 4.dp)
                .padding(16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Column(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = "Page: ${pdfState.currentPage}/${pdfState.pdfPageCount}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}