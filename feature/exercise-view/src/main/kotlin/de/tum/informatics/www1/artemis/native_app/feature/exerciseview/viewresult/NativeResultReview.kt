package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.viewresult

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.*
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.R
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

internal sealed interface ReviewLoad {
    data object Loading : ReviewLoad
    data class Ready(val content: ReviewContent) : ReviewLoad
    data class Failed(val denied: Boolean) : ReviewLoad
}

internal fun isReviewAccessDenied(error: Throwable): Boolean = when (error) {
    is ReviewHttpException -> error.status == 401 || error.status == 403
    is ClientRequestException -> error.response.status.value in listOf(401, 403)
    else -> false
}

@Composable
internal fun NativeResultReview(modifier: Modifier, exercise: Exercise, target: ReviewTarget, session: ArtemisContext.LoggedIn) {
    val ktor: KtorProvider = koinInject()
    val service = remember(ktor) { ResultReviewService(ktor) }
    var retry by remember { mutableIntStateOf(0) }
    var state by remember(session, target) { mutableStateOf<ReviewLoad>(ReviewLoad.Loading) }
    LaunchedEffect(session, target, retry) {
        state = ReviewLoad.Loading
        state = try { ReviewLoad.Ready(service.load(session, exercise, target)) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) { ReviewLoad.Failed(isReviewAccessDenied(error)) }
    }
    when (val current = state) {
        ReviewLoad.Loading -> Box(modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { CircularProgressIndicator() }
        is ReviewLoad.Failed -> ReviewMessage(modifier, if (current.denied) R.string.review_access_unavailable else R.string.review_load_error) { retry++ }
        is ReviewLoad.Ready -> ResultReviewBody(modifier, exercise, target, current.content, service, session) {
            state = ReviewLoad.Failed(true)
        }
    }
}

@Composable
internal fun ReviewMessage(modifier: Modifier = Modifier, message: Int, retry: (() -> Unit)? = null) {
    Column(modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(message), style = MaterialTheme.typography.bodyLarge)
        retry?.let { TextButton(onClick = it) { Text(stringResource(R.string.review_retry)) } }
    }
}

@Composable
private fun ResultReviewBody(
    modifier: Modifier, exercise: Exercise, target: ReviewTarget, content: ReviewContent,
    service: ResultReviewService, session: ArtemisContext.LoggedIn, onDenied: () -> Unit,
) {
    var selected by remember(target) { mutableStateOf<Int?>(null) }
    var tab by remember { mutableIntStateOf(0) }
    Column(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(exercise.title.orEmpty(), style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
            val score = reviewScore(target.result.score, exercise.maxPoints, exercise.course?.accuracyOfScores?.toInt() ?: 1)
            Text(score ?: stringResource(R.string.review_score_unavailable), style = MaterialTheme.typography.titleMedium)
            val participation = exercise.studentParticipations.orEmpty().firstOrNull { it.id == target.participationId }
            val practice = (participation as? de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.StudentParticipation)?.testRun == true
            val submittedAt = target.submission?.submissionDate
            val dueDate = exercise.getDueDate(participation)
            val late = submittedAt != null && dueDate != null && submittedAt > dueDate
            val assessmentState = reviewAssessmentState(exercise, target.result, practice, kotlin.time.Clock.System.now())
            val stateLabel = when (assessmentState) {
                ReviewAssessmentState.FINAL -> null
                ReviewAssessmentState.PRELIMINARY -> R.string.review_preliminary
                ReviewAssessmentState.AI_PENDING -> R.string.review_ai_pending
                ReviewAssessmentState.AI_FAILED -> R.string.review_ai_failed
                ReviewAssessmentState.AI_TIMED_OUT -> R.string.review_ai_timed_out
            }
            Text(
                listOfNotNull(
                    if (practice) stringResource(R.string.review_practice) else if (target.result.rated == false) stringResource(R.string.review_unrated) else if (target.result.rated == true) stringResource(R.string.review_rated) else null,
                    stateLabel?.let { stringResource(it) },
                    if (late) stringResource(R.string.review_late) else null,
                    if ((target.submission as? de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.ProgrammingSubmission)?.buildFailed == true) stringResource(R.string.review_build_failed) else null,
                    reviewDate(content.submission?.submissionDate ?: target.submission?.submissionDate?.toString())
                ).joinToString(" · "), style = MaterialTheme.typography.bodySmall,
            )
        }
        if (exercise is ProgrammingExercise && target.result.testCaseCount != null && target.result.passedTestCaseCount != null) {
            Text(stringResource(R.string.review_test_summary, target.result.passedTestCaseCount!!, target.result.testCaseCount!!), Modifier.padding(horizontal = 20.dp, vertical = 4.dp), style = MaterialTheme.typography.bodyMedium)
        }
        HorizontalDivider()
        BoxWithConstraints(Modifier.weight(1f)) {
            val wide = maxWidth >= 840.dp && LocalDensity.current.fontScale < 1.5f
            val work: @Composable (Modifier) -> Unit = { pane ->
                Column(pane.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ReviewHeading(R.string.review_assessed_work)
                    when (exercise) {
                        is TextExercise -> AssessedText(content, selected) { selected = it }
                        is FileUploadExercise -> AssessedFile(content.submission, service, session, onDenied)
                        is ProgrammingExercise -> ProgrammingSource(content, target, service, session, selected, onDenied)
                        is ModelingExercise -> {
                            ModelReviewCanvas(content.submission?.model, exercise.diagramType?.name, content.feedback, selected?.let { content.feedback.getOrNull(it)?.reference }) { reference ->
                                selected = content.feedback.indexOfFirst { it.reference == reference }.takeIf { it >= 0 }
                            }
                            content.submission?.explanationText?.takeIf { it.isNotBlank() }?.let { SelectionContainer { Text(it) } }
                        }
                        else -> Text(stringResource(R.string.review_source_unavailable))
                    }
                    PublishedSolution(exercise)
                }
            }
            val feedback: @Composable (Modifier) -> Unit = { pane ->
                Column(pane.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReviewHeading(R.string.review_feedback)
                    FeedbackList(content, exercise is ProgrammingExercise, selected, { selected = it; if (!wide && content.feedback.getOrNull(it)?.reference != null && exercise !is FileUploadExercise) tab = 0 }, service, session, onDenied)
                    if (exercise is ProgrammingExercise) ProgrammingLogs(target, service, session, onDenied)
                }
            }
            if (wide) Row(Modifier.fillMaxSize()) {
                work(Modifier.weight(1.15f).fillMaxHeight())
                VerticalDivider()
                feedback(Modifier.weight(1f).fillMaxHeight())
            } else Column(Modifier.fillMaxSize()) {
                SecondaryTabRow(selectedTabIndex = tab) {
                    Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(R.string.review_assessed_work)) })
                    Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(R.string.review_feedback)) })
                }
                if (tab == 0) work(Modifier.fillMaxSize()) else feedback(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
internal fun ReviewHeading(label: Int) {
    Text(stringResource(label), style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
}

@Composable
private fun AssessedText(content: ReviewContent, selected: Int?, onSelect: (Int) -> Unit) {
    val submission = content.submission
    val text = submission?.text
    if (text == null) { Text(stringResource(R.string.review_source_unavailable)); return }
    val highlight = MaterialTheme.colorScheme.secondaryContainer
    val active = MaterialTheme.colorScheme.primaryContainer
    val foreground = MaterialTheme.colorScheme.onSurface
    val answer = buildAnnotatedString {
        append(text)
        content.feedback.forEachIndexed { index, feedback ->
            submission.rangeFor(feedback)?.let { range ->
                addStyle(SpanStyle(background = if (index == selected) active else highlight, color = foreground), range.first, range.last + 1)
                addLink(LinkAnnotation.Clickable("feedback-$index") { onSelect(index) }, range.first, range.last + 1)
            }
        }
    }
    SelectionContainer { Text(answer, style = MaterialTheme.typography.bodyLarge) }
    selected?.let { content.feedback.getOrNull(it) }?.let { feedback ->
        HorizontalDivider()
        FeedbackText(feedback)
    }
}

@Composable
private fun FeedbackList(
    content: ReviewContent, programming: Boolean, selected: Int?, onSelect: (Int) -> Unit,
    service: ResultReviewService, session: ArtemisContext.LoggedIn, onDenied: () -> Unit,
) {
    if (content.feedback.isEmpty()) Text(stringResource(R.string.review_no_feedback))
    val groups = if (programming) content.feedback.withIndex().groupBy { it.value.group } else mapOf(FeedbackGroup.MANUAL to content.feedback.withIndex().toList())
    val orderedGroups = if (programming) FeedbackGroup.entries.mapNotNull { group -> groups[group]?.let { group to it } } else groups.toList()
    orderedGroups.forEach { (group, groupItems) ->
        val items = if (group == FeedbackGroup.TESTS) groupItems.sortedBy { when (it.value.positive) { false -> 0; null -> 1; true -> 2 } } else groupItems
        if (programming) ReviewHeading(when (group) {
            FeedbackGroup.TESTS -> R.string.review_tests
            FeedbackGroup.STATIC_ANALYSIS -> R.string.review_static_analysis
            FeedbackGroup.POLICY -> R.string.review_policy
            FeedbackGroup.MANUAL -> R.string.review_manual
        })
        items.forEach { (index, feedback) ->
            Surface(
                modifier = Modifier.fillMaxWidth().semantics { this.selected = selected == index }.clickable { onSelect(index) },
                color = if (selected == index) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FeedbackText(feedback)
                    val range = content.submission?.rangeFor(feedback)
                    if (!programming && feedback.reference != null) {
                        Text(stringResource(if (range != null || content.submission?.model != null) R.string.review_located_feedback else R.string.review_unlocated_feedback), style = MaterialTheme.typography.labelMedium)
                    }
                    LongFeedback(feedback, service, session, onDenied)
                }
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun FeedbackText(feedback: ReviewFeedback) {
    val status = if (feedback.group == FeedbackGroup.TESTS) when (feedback.positive) { true -> R.string.review_test_passed; false -> R.string.review_test_failed; null -> R.string.review_test_unknown }
    else when (feedback.positive) { true -> R.string.review_positive; false -> R.string.review_negative; null -> R.string.review_note }
    Text(listOfNotNull(stringResource(status), feedback.credits?.let { stringResource(R.string.review_points, it) }).joinToString(" · "), style = MaterialTheme.typography.labelMedium)
    feedback.title?.let { SelectionContainer { Text(it, style = MaterialTheme.typography.titleSmall) } }
    if (feedback.message.isNotBlank()) MarkdownText(markdown = feedback.previewMessage, selectable = true)
    feedback.codeLocation()?.let { location ->
        SelectionContainer { Text(stringResource(R.string.review_code_location, location.file, location.firstLine + 1, location.lastLine + 1), style = MaterialTheme.typography.bodySmall) }
    }
    feedback.diagnostic?.let { diagnostic ->
        SelectionContainer { Text(listOfNotNull(diagnostic.filePath, diagnostic.startLine?.toString(), diagnostic.priority, diagnostic.rule).joinToString(" · "), style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun LongFeedback(feedback: ReviewFeedback, service: ResultReviewService, session: ArtemisContext.LoggedIn, onDenied: () -> Unit) {
    if (!feedback.hasLongFeedbackText || feedback.id == null) return
    val scope = rememberCoroutineScope()
    var loading by remember(feedback.id) { mutableStateOf(false) }
    var failed by remember(feedback.id) { mutableStateOf(false) }
    var full by remember(feedback.id) { mutableStateOf<String?>(null) }
    var expanded by remember(feedback.id) { mutableStateOf(false) }
    TextButton(enabled = !loading, onClick = {
        if (full != null) expanded = !expanded
        else {
            loading = true
            scope.launch {
                try { full = service.longFeedback(session, feedback.id); failed = false; expanded = true }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (error: Exception) { failed = true; if (isReviewAccessDenied(error)) onDenied() }
                finally { loading = false }
            }
        }
    }) { Text(stringResource(if (failed) R.string.review_long_retry else if (loading) R.string.review_loading else if (expanded) R.string.review_long_collapse else R.string.review_long_expand)) }
    if (expanded) full?.let { SelectionContainer { Text(it, style = MaterialTheme.typography.bodyMedium) } }
}

@Composable
private fun PublishedSolution(exercise: Exercise) {
    // These fields are filtered by the authorized student exercise endpoint, independently of assessment release.
    val solution = when (exercise) { is TextExercise -> exercise.exampleSolution; is FileUploadExercise -> exercise.exampleSolution; else -> null }
    if (!solution.isNullOrBlank()) {
        HorizontalDivider()
        ReviewHeading(R.string.review_example_solution)
        MarkdownText(markdown = solution, selectable = true)
    }
}
