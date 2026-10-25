package de.tum.informatics.www1.artemis.native_app.core.ui.compose

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
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.runtime.key
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import de.tum.informatics.www1.artemis.native_app.core.ui.media.MediaProgressStore
import de.tum.informatics.www1.artemis.native_app.core.ui.media.initialPdfPageIndex
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.R
import de.tum.informatics.www1.artemis.native_app.core.ui.endOfPagePadding
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.PdfFile
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.render.HorizontalPdfView
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.render.VerticalPdfView
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.render.state.HorizontalPdfReaderState
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.render.state.PdfReaderState
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.render.state.VerticalPdfReaderState
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.render.state.rememberHorizontalPdfReaderState
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.render.state.rememberVerticalPdfReaderState

@Composable
fun ArtemisPdfView(
    modifier: Modifier,
    pdfFile: PdfFile,
    dismiss: () -> Unit,
    progressKey: String? = null,
    initialPage: Int? = null
) {
    val context = LocalContext.current
    val store = remember(context) { MediaProgressStore(context) }
    var currentPage by rememberSaveable(progressKey, initialPage) {
        mutableStateOf(initialPage ?: progressKey?.let { store.read(it)?.toInt() } ?: 1)
    }
    var attempt by remember { mutableStateOf(0) }
    key(pdfFile, attempt) {
        ArtemisPdfContent(modifier, pdfFile, dismiss, currentPage, { page ->
            currentPage = page
            progressKey?.let { store.save(it, page.toLong()) }
        }, { attempt++ })
    }
}

@Composable
private fun ArtemisPdfContent(
    modifier: Modifier,
    pdfFile: PdfFile,
    dismiss: () -> Unit,
    initialPage: Int,
    onPageChanged: (Int) -> Unit,
    onRetry: () -> Unit
) {
    val isVertical = remember { mutableStateOf(true) }
    val context = LocalContext.current

    val verticalPdfState = rememberVerticalPdfReaderState(
        pdfFile = pdfFile,
        isZoomEnabled = true,
    )
    val horizontalPdfState = rememberHorizontalPdfReaderState(
        pdfFile = pdfFile,
        isZoomEnabled = true,
    )

    var pdfState = if (isVertical.value) verticalPdfState else horizontalPdfState

    var showMenu by remember { mutableStateOf(false) }
    LaunchedEffect(pdfState, pdfState.pdfPageCount) {
        val activeState = pdfState
        if (activeState.pdfPageCount > 0) {
            val index = initialPdfPageIndex(initialPage, null, activeState.pdfPageCount)
            if (activeState is VerticalPdfReaderState) activeState.lazyState.scrollToItem(index)
            else activeState.currentPage = index + 1
            snapshotFlow { if (activeState is VerticalPdfReaderState) activeState.lazyState.firstVisibleItemIndex + 1 else activeState.currentPage }.collect { page ->
                if (page > 0) onPageChanged(page)
            }
        }
    }

    Box(
        modifier = modifier.padding(8.dp),
    ) {
        Column {
            pdfState.file?.let {
                Text(
                    text = pdfFile.filename,
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
                .endOfPagePadding()
        ) {
            if (showMenu) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            FloatingActionButton(
                onClick = { showMenu = !showMenu },
                modifier = Modifier
            ) {
                Icon(imageVector = Icons.Default.MoreHoriz, contentDescription = stringResource(R.string.pdf_view_actions))
            }

            PdfActionDropDownMenu(
                modifier = Modifier,
                isExpanded = showMenu,
                onDismissRequest = { showMenu = false },
                downloadPdf = {
                    showMenu = false
                    pdfState.file?.let { pdfFile.download(context, it) }
                },
                sharePdf = {
                    showMenu = false
                    pdfState.file?.let { pdfFile.share(context, it) }
                },
                toggleViewMode = {
                    showMenu = false
                    val page = if (pdfState is VerticalPdfReaderState) (pdfState as VerticalPdfReaderState).lazyState.firstVisibleItemIndex + 1
                        else pdfState.currentPage.coerceAtLeast(1)
                    onPageChanged(page)
                    pdfState = if (isVertical.value) horizontalPdfState else verticalPdfState
                    pdfState.currentPage = page
                    isVertical.value = !isVertical.value
                }
            )
        }

        if (!pdfState.isLoaded && pdfState.error == null) {
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
            PageCountText(
                modifier = Modifier,
                pdfState = pdfState
            )
        }
        if (pdfState.error != null) {
            Column(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.pdf_view_error_loading))
                Text(stringResource(R.string.lecture_media_access_recovery))
                Button(onClick = onRetry) { Text(stringResource(R.string.lecture_media_retry)) }
                TextButton(onClick = dismiss) { Text(stringResource(R.string.lecture_media_close)) }
            }
        }
    }

}

@Composable
private fun PageCountText(
    modifier: Modifier,
    pdfState: PdfReaderState
) {
    Box(
        modifier = modifier
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

@Composable
private fun PdfActionDropDownMenu(
    modifier: Modifier,
    isExpanded: Boolean,
    onDismissRequest: () -> Unit,
    downloadPdf: () -> Unit,
    sharePdf: () -> Unit,
    toggleViewMode: () -> Unit,
) {
    DropdownMenu(
        modifier = modifier,
        expanded = isExpanded,
        onDismissRequest = onDismissRequest
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null
                )
            },
            text = { Text(stringResource(R.string.pdf_view_download_menu_item)) },
            onClick = downloadPdf
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null
                )
            },
            text = { Text(stringResource(R.string.pdf_view_share_menu_item)) },
            onClick = sharePdf
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
            onClick = toggleViewMode
        )
    }
}