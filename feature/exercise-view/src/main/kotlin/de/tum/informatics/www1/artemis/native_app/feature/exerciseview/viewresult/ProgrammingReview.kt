package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.viewresult

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

internal data class ReviewCodeLocation(val file: String, val firstLine: Int, val lastLine: Int)

/** Manual code references are zero-based; static-analysis locations are one-based. */
internal fun ReviewFeedback.codeLocation(): ReviewCodeLocation? {
    diagnostic?.let { issue ->
        val line = issue.startLine?.takeIf { it > 0 } ?: return null
        return issue.filePath?.let { ReviewCodeLocation(it, line - 1, (issue.endLine ?: line).coerceAtLeast(line) - 1) }
    }
    val match = Regex("^file:(.+)_line:(\\d+)(?:-(\\d+))?$").matchEntire(reference.orEmpty()) ?: return null
    val start = match.groupValues[2].toIntOrNull() ?: return null
    val end = match.groupValues[3].toIntOrNull() ?: start
    return ReviewCodeLocation(match.groupValues[1], start, end).takeIf { end >= start }
}

@Composable
internal fun ProgrammingSource(content: ReviewContent, target: ReviewTarget, service: ResultReviewService, session: ArtemisContext.LoggedIn, selected: Int?, onDenied: () -> Unit) {
    val commit = content.submission?.commitHash
    if (commit == null || !commit.matches(Regex("[a-fA-F0-9]{40}"))) { Text(stringResource(R.string.review_commit_unavailable)); return }
    SelectionContainer { Text(stringResource(R.string.review_assessed_commit, commit), style = MaterialTheme.typography.bodySmall) }
    var files by remember(commit) { mutableStateOf<List<String>?>(null) }
    var failed by remember(commit) { mutableStateOf(false) }
    var retry by remember(commit) { mutableIntStateOf(0) }
    var filename by remember(commit) { mutableStateOf<String?>(null) }
    var source by remember(commit) { mutableStateOf<String?>(null) }
    var loading by remember(commit) { mutableStateOf(false) }
    val location = selected?.let { content.feedback.getOrNull(it)?.codeLocation() }
    LaunchedEffect(commit, retry) {
        failed = false
        try { files = service.commitFiles(session, target, commit); if (filename == null) filename = files?.firstOrNull() }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) { failed = true; if (isReviewAccessDenied(error)) onDenied() }
    }
    LaunchedEffect(location, files) { location?.file?.takeIf { it in files.orEmpty() }?.let { filename = it } }
    LaunchedEffect(filename, retry) {
        source = null
        filename?.let { name ->
            loading = true
            try { source = service.commitFile(session, target, commit, name) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { failed = true; if (isReviewAccessDenied(error)) onDenied() }
            finally { loading = false }
        }
    }
    if (failed) {
        Text(stringResource(R.string.review_source_unavailable))
        TextButton(onClick = { retry++ }) { Text(stringResource(R.string.review_retry)) }
    }
    var expanded by remember { mutableStateOf(false) }
    if (!files.isNullOrEmpty()) {
        Box {
            OutlinedButton(onClick = { expanded = true }) { Text(filename.orEmpty()) }
            DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                files.orEmpty().forEach { file -> DropdownMenuItem(text = { Text(file) }, onClick = { filename = file; expanded = false }) }
            }
        }
    }
    if (loading || files == null && !failed) LinearProgressIndicator(Modifier.fillMaxWidth())
    source?.let { code ->
        SelectionContainer {
            Column(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                code.lines().forEachIndexed { index, line ->
                    val highlight = location != null && location.file == filename && index in location.firstLine..location.lastLine
                    Text("${index + 1}  $line", Modifier.fillMaxWidth().then(if (highlight) Modifier.background(MaterialTheme.colorScheme.secondaryContainer) else Modifier), fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
internal fun ProgrammingLogs(target: ReviewTarget, service: ResultReviewService, session: ArtemisContext.LoggedIn, onDenied: () -> Unit) {
    val scope = rememberCoroutineScope()
    var logs by remember(target) { mutableStateOf<List<ReviewBuildLog>?>(null) }
    var loading by remember(target) { mutableStateOf(false) }
    var failed by remember(target) { mutableStateOf(false) }
    var expanded by remember(target) { mutableStateOf(false) }
    TextButton(enabled = !loading, onClick = {
        expanded = !expanded
        if (logs == null || failed) {
            expanded = true
            loading = true
            scope.launch {
                try { logs = service.buildLogs(session, target); failed = false }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (error: Exception) { failed = true; if (isReviewAccessDenied(error)) onDenied() }
                finally { loading = false }
            }
        }
    }) { Text(stringResource(if (loading) R.string.review_loading else if (failed) R.string.review_logs_retry else if (expanded) R.string.review_logs_hide else R.string.review_logs_show)) }
    if (expanded) {
        if (failed) Text(stringResource(R.string.review_logs_error))
        else if (logs?.isEmpty() == true) Text(stringResource(R.string.review_logs_empty))
        else logs?.let { entries -> SelectionContainer { Text(entries.joinToString("\n") { listOfNotNull(it.time, it.log).joinToString("  ") }, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall) } }
    }
}
