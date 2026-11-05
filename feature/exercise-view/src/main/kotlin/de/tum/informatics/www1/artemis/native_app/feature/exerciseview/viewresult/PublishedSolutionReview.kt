package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.viewresult

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.FileUploadExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ModelingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ProgrammingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.TextExercise
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.R
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** A published solution has its own student authorization; a released assessment never grants access. */
@Composable
internal fun PublishedSolutionReview(exercise: Exercise, service: ResultReviewService, session: ArtemisContext.LoggedIn, onDenied: () -> Unit) {
    val exerciseId = exercise.id ?: return
    if (exercise.exampleSolutionPublicationDate == null) return
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    var solution by remember(session, exerciseId) { mutableStateOf<Exercise?>(null) }
    var loading by remember(session, exerciseId) { mutableStateOf(false) }
    var unavailable by remember(session, exerciseId) { mutableStateOf(false) }
    val browserHandoff = exercise is ProgrammingExercise
    val browserUrl = exercise.course?.id?.let { reviewExampleSolutionUrl(session.serverUrl, it, exerciseId) }
    HorizontalDivider()
    ReviewHeading(R.string.review_example_solution)
    if (browserHandoff) Text(stringResource(R.string.review_solution_browser_signin))
    if (solution == null || browserHandoff) OutlinedButton(
        enabled = !loading && (!browserHandoff || browserUrl != null),
        onClick = {
            loading = true
            scope.launch {
                try {
                    val authorized = service.publishedSolution(session, exerciseId)
                    require(authorized::class == exercise::class)
                    solution = authorized
                    unavailable = false
                    if (browserHandoff && browserUrl != null) uriHandler.openUri(browserUrl)
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (error: Exception) {
                    unavailable = true
                    // A withheld solution (403) does not revoke independently authorized result feedback.
                    val unauthorized = when (error) {
                        is ClientRequestException -> error.response.status.value == 401
                        is ReviewHttpException -> error.status == 401
                        else -> false
                    }
                    if (unauthorized) onDenied()
                } finally { loading = false }
            }
        }
    ) { Text(stringResource(if (loading) R.string.review_loading else if (browserHandoff) R.string.review_solution_browser_open else R.string.review_solution_show)) }
    if (unavailable) Text(stringResource(R.string.review_solution_unavailable))
    when (val published = solution) {
        is TextExercise -> published.exampleSolution?.let { MarkdownText(markdown = it, selectable = true) }
        is FileUploadExercise -> published.exampleSolution?.let { MarkdownText(markdown = it, selectable = true) }
        is ModelingExercise -> {
            published.exampleSolutionModel?.let { model ->
                ModelReviewCanvas(model, published.diagramType?.name, emptyList(), null) {}
            }
            published.exampleSolutionExplanation?.let { MarkdownText(markdown = it, selectable = true) }
        }
        else -> Unit
    }
}
