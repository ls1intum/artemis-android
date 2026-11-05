package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.viewresult

import android.content.Intent
import android.graphics.BitmapFactory
import android.webkit.MimeTypeMap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.PdfFile
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.render.VerticalPdfView
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.render.state.VerticalPdfReaderState
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
internal fun AssessedFile(submission: ReviewSubmission?, service: ResultReviewService, session: ArtemisContext.LoggedIn, onDenied: () -> Unit) {
    val context = LocalContext.current
    val url = remember(submission, session.serverUrl) { reviewFileUrl(session.serverUrl, submission?.filePath) }
    if (url == null) { Text(stringResource(R.string.review_file_unavailable)); return }
    val filename = remember(url) { android.net.Uri.parse(url).lastPathSegment ?: "submission" }
    val extension = filename.substringAfterLast('.', "").lowercase()
    var requested by remember(url) { mutableStateOf(false) }
    var retry by remember(url) { mutableIntStateOf(0) }
    var loading by remember(url) { mutableStateOf(false) }
    var failed by remember(url) { mutableStateOf(false) }
    var downloaded by remember(url) { mutableStateOf<File?>(null) }
    DisposableEffect(url) { onDispose { downloaded?.delete() } }
    LaunchedEffect(url, requested, retry) {
        if (requested) {
            loading = true
            try {
                val bytes = service.file(session, url)
                val file = withContext(Dispatchers.IO) {
                    File.createTempFile("assessed-${submission?.id}-", ".$extension", context.cacheDir).apply { writeBytes(bytes) }
                }
                downloaded?.delete()
                downloaded = file
                failed = false
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { failed = true; if (isReviewAccessDenied(error)) onDenied() }
            finally { loading = false }
        }
    }
    Text(filename, style = MaterialTheme.typography.titleSmall)
    val file = downloaded
    if (file == null) {
        if (failed) Text(stringResource(R.string.review_file_error))
        OutlinedButton(enabled = !loading, onClick = { requested = true; retry++ }) {
            Text(stringResource(if (loading) R.string.review_loading else if (failed) R.string.review_retry else R.string.review_open_file))
        }
    } else when (extension) {
        "pdf" -> {
            // Reuse the zoomable native reader, with an already authenticated, transient local artifact.
            // Its state is not saved: neither credentials nor protected work enter a saved-state bundle.
            val pdf = remember(file) { PdfFile("", "", filename, localFile = file) }
            val state = remember(file) { VerticalPdfReaderState(pdf, isZoomEnabled = true) }
            if (state.error != null) Text(stringResource(R.string.review_file_error))
            else {
                if (state.pdfPageCount > 0) Text(stringResource(R.string.review_pdf_pages, state.firstVisiblePage, state.pdfPageCount), style = MaterialTheme.typography.labelMedium)
                VerticalPdfView(state, Modifier.fillMaxWidth().height(540.dp))
            }
        }
        "png", "jpg", "jpeg", "webp", "gif" -> {
            val bitmap = remember(file) { BitmapFactory.decodeFile(file.path)?.asImageBitmap() }
            if (bitmap != null) Image(bitmap, contentDescription = filename, modifier = Modifier.fillMaxWidth())
            else Text(stringResource(R.string.review_file_error))
        }
        else -> OutlinedButton(onClick = {
            runCatching {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri, MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
            }.onFailure { failed = true }
        }) { Text(stringResource(R.string.review_open_file)) }
    }
    if (failed && file != null) Text(stringResource(R.string.review_file_error))
}
