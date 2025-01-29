package de.tum.informatics.www1.artemis.native_app.feature.quiz.participation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.DestructiveMarkdownTextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.common.ButtonWithLoadingAnimation
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.ExerciseColors
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.KSerializableNavType
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.animatedComposable
import de.tum.informatics.www1.artemis.native_app.feature.quiz.QuizType
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R
import de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result.ViewQuizResultScreen
import kotlinx.coroutines.Deferred
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import kotlin.reflect.typeOf

@Serializable
private data class QuizParticipationScreen(
    val courseId: Long,
    val exerciseId: Long,
    val quizType: QuizType.WorkableQuizType = QuizType.Live
)

fun NavController.navigateToQuizParticipation(
    courseId: Long,
    exerciseId: Long,
    quizType: QuizType.WorkableQuizType
) {
    navigate(QuizParticipationScreen(courseId, exerciseId, quizType))
}

fun NavGraphBuilder.quizParticipation(onLeaveQuiz: () -> Unit) {
    animatedComposable<QuizParticipationScreen>(
        typeMap = mapOf(
            typeOf<QuizType.WorkableQuizType>() to KSerializableNavType(
                isNullableAllowed = false,
                QuizType.WorkableQuizType.serializer()
            )
        ),
        deepLinks = listOf(
            navDeepLink {
                uriPattern = "artemis://quiz_participation/{courseId}/{exerciseId}"
            }
        )
    ) { backStackEntry ->
        val screen = backStackEntry.toRoute<QuizParticipationScreen>()
        val courseId = screen.courseId
        val exerciseId = screen.exerciseId
        val quizType = screen.quizType

        val jsonProvider: JsonProvider = koinInject()

        // When this is set, instead of participating a result screen is displayed
        var loadedViewableQuizType: QuizType.ViewableQuizType? by rememberSaveable(
            stateSaver = Saver(save = {
                jsonProvider.applicationJsonConfiguration.encodeToString(it)
            }, restore = {
                jsonProvider.applicationJsonConfiguration.decodeFromString(it)
            })
        ) { mutableStateOf(null) }

        val currentViewableQuizType = loadedViewableQuizType
        if (currentViewableQuizType == null) {
            val viewModel: QuizParticipationViewModel =
                koinViewModel { parametersOf(courseId, exerciseId, quizType) }

            QuizParticipationScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onNavigateUp = onLeaveQuiz,
                onNavigateToInspectResult = { loadedViewableQuizType = it }
            )
        } else {
            ViewQuizResultScreen(
                modifier = Modifier.fillMaxSize(),
                exerciseId = exerciseId,
                quizType = currentViewableQuizType,
                onNavigateUp = onLeaveQuiz
            )
        }
    }
}

@Composable
internal fun QuizParticipationScreen(
    modifier: Modifier,
    viewModel: QuizParticipationViewModel,
    onNavigateToInspectResult: (QuizType.ViewableQuizType) -> Unit,
    onNavigateUp: () -> Unit
) {
    val exerciseDataState = viewModel.quizExerciseDataState.collectAsState().value
    val isWaitingForQuizStart by viewModel.waitingForQuizStart.collectAsState(initial = true)
    val hasQuizEnded by viewModel.quizEndedStatus.collectAsState(initial = false)

    val haveAllQuestionsBeenAnswered by viewModel.haveAllQuestionsBeenAnswered.collectAsState(
        initial = false
    )

    var displayLeaveQuizDialog by rememberSaveable { mutableStateOf(false) }

    var displaySubmitDialog: Boolean by rememberSaveable { mutableStateOf(false) }
    var submissionDeferred: Deferred<Boolean>? by remember { mutableStateOf(null) }
    var displaySubmissionFailedDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val latestWebsocketSubmission by viewModel.latestWebsocketSubmission.collectAsState(initial = null)

    AwaitDeferredCompletion(job = submissionDeferred) { successful ->
        if (!successful) {
            displaySubmissionFailedDialog = true
        }

        submissionDeferred = null
    }

    val initSubmit = {
        displaySubmitDialog = false

        submissionDeferred = viewModel.submit()
    }

    // Called when the user tries to leave this quiz
    val onRequestLeave = {
        if (isWaitingForQuizStart || hasQuizEnded) onNavigateUp()
        else {
            displayLeaveQuizDialog = true
        }
    }

    BackHandler(onBack = onRequestLeave)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = exerciseDataState.bind { it.title.orEmpty() }.orElse("")) },
                navigationIcon = {
                    IconButton(onClick = onRequestLeave) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                },
                actions = {
                    if (!isWaitingForQuizStart && !hasQuizEnded) {
                        ButtonWithLoadingAnimation(
                            modifier = Modifier,
                            isLoading = submissionDeferred != null,
                            onClick = { displaySubmitDialog = true },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = ExerciseColors.Quiz.submitButton,
                                contentColor = ExerciseColors.Quiz.submitButtonText
                            )
                        ) {
                            Text(text = stringResource(id = R.string.quiz_participation_submit_button))
                        }
                    }
                }
            )
        }
    ) { padding ->
        QuizParticipationUi(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            viewModel = viewModel,
            onNavigateToInspectResult = onNavigateToInspectResult,
            onNavigateUp = onNavigateUp
        )

        if (displaySubmitDialog) {
            val textRes = if (haveAllQuestionsBeenAnswered) {
                R.string.quiz_participation_submit_dialog_message
            } else R.string.quiz_participation_submit_dialog_message_not_all_answered

            DestructiveMarkdownTextAlertDialog(
                title = stringResource(id = R.string.quiz_participation_submit_dialog_title),
                text = stringResource(id = textRes),
                confirmButtonText = stringResource(id = R.string.quiz_participation_submit_dialog_positive),
                dismissButtonText = stringResource(id = R.string.quiz_participation_submit_dialog_negative),
                onPressPositiveButton = initSubmit,
                onDismissRequest = { displaySubmitDialog = false }
            )
        }

        if (submissionDeferred != null) {
            AlertDialog(
                onDismissRequest = {
                    submissionDeferred?.cancel()
                    submissionDeferred = null
                },
                text = {
                    Text(text = stringResource(id = R.string.quiz_participation_submitting_dialog_message))
                },
                confirmButton = {}
            )
        }

        if (displaySubmissionFailedDialog) {
            TextAlertDialog(
                title = stringResource(id = R.string.quiz_participation_submit_failed_dialog_title),
                text = stringResource(id = R.string.quiz_participation_submit_failed_dialog_message),
                confirmButtonText = stringResource(id = R.string.quiz_participation_submit_failed_dialog_positive),
                dismissButtonText = stringResource(id = R.string.quiz_participation_submit_failed_dialog_negative),
                onPressPositiveButton = initSubmit,
                onDismissRequest = { displaySubmissionFailedDialog = false }
            )
        }

        if (displayLeaveQuizDialog) {
            val textRes =
                if (latestWebsocketSubmission?.isFailure != true) R.string.quiz_participation_leave_without_submit_dialog_message
                else R.string.quiz_participation_leave_without_submit_dialog_message_unsaved_changes

            DestructiveMarkdownTextAlertDialog(
                title = stringResource(id = R.string.quiz_participation_leave_without_submit_dialog_title),
                text = stringResource(id = textRes),
                confirmButtonText = stringResource(id = R.string.quiz_participation_leave_without_submit_dialog_positive),
                dismissButtonText = stringResource(id = R.string.quiz_participation_leave_without_submit_dialog_negative),
                onPressPositiveButton = onNavigateUp,
                onDismissRequest = { displayLeaveQuizDialog = false }
            )
        }
    }
}