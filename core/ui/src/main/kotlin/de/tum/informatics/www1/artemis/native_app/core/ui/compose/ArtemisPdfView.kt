package de.tum.informatics.www1.artemis.native_app.core.ui.compose

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.R
import de.tum.informatics.www1.artemis.native_app.core.ui.pdf.PdfFile
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
    val context = LocalContext.current
    val pdfFile = PdfFile(url, authToken, filename)

    val verticalPdfState = rememberVerticalPdfReaderState(
        pdfFile = pdfFile,
        isZoomEnabled = true,
    )
    val horizontalPdfState = rememberHorizontalPdfReaderState(
        pdfFile = pdfFile,
        isZoomEnabled = true,
    )

    val pdfState = if (isVertical.value) verticalPdfState else horizontalPdfState

    val showMenu = remember { mutableStateOf(false) }

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
            stringResource(id = R.string.pdf_view_error_loading),
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

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            if (showMenu.value) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            FloatingActionButton(
                onClick = { showMenu.value = !showMenu.value },
                modifier = Modifier
            ) {
                Icon(imageVector = Icons.Default.MoreHoriz, contentDescription = null)
            }

            DropdownMenu(
                modifier = Modifier,
                expanded = showMenu.value,
                onDismissRequest = { showMenu.value = false }
            ) {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null
                        )
                    },
                    text = { Text(stringResource(R.string.pdf_view_download_menu_item)) },
                    onClick = {
                        showMenu.value = false
                        verticalPdfState.file?.let { pdfFile.downloadPdf(context) }
                    }
                )
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null
                        )
                    },
                    text = { Text(stringResource(R.string.pdf_view_share_menu_item)) },
                    onClick = {
                        showMenu.value = false
                        verticalPdfState.file?.let { pdfFile.sharePdf(context, it) }
                    }
                )
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            modifier = Modifier.height(19.dp),
                            painter = painterResource(id = R.drawable.rotate),
                            contentDescription = null
                        )
                    },
                    text = { Text(stringResource(R.string.pdf_view_rotate_menu_item)) },
                    onClick = {
                        showMenu.value = false
                        isVertical.value = !isVertical.value
                    }
                )
            }
        }

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
                .align(Alignment.TopStart)
                .padding(top = 54.dp, end = 4.dp)
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
                    text = stringResource(
                        R.string.pdf_view_page_count,
                        pdfState.currentPage,
                        pdfState.pdfPageCount
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}