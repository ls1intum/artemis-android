package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.koin.androidx.compose.getStateViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavController.navigateToQuizParticipation(
    courseId: Long,
    exerciseId: Long,
    quizType: QuizType
) {
    navigate("quiz-participation/$courseId/$exerciseId/$quizType")
}

fun NavGraphBuilder.quizParticipation(onLeaveQuiz: () -> Unit) {
    composable(
        "quiz-participation/{courseId}/{exerciseId}/{quizType}",
        arguments = listOf(
            navArgument("courseId") {
                type = NavType.LongType
            },
            navArgument("exerciseId") {
                type = NavType.LongType
            },
            navArgument("quizType") {
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        val courseId = backStackEntry.arguments?.getLong("courseId")
        val exerciseId = backStackEntry.arguments?.getLong("exerciseId")
        val quizTypeString = backStackEntry.arguments?.getString("quizType")

        checkNotNull(courseId)
        checkNotNull(exerciseId)
        checkNotNull(quizTypeString)

        val quizType: QuizType = QuizType.valueOf(quizTypeString)

        val viewModel: QuizParticipationViewModel =
            getStateViewModel { parametersOf(courseId, exerciseId, quizType) }

        QuizParticipationScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = viewModel,
            onNavigateUp = onLeaveQuiz
        )
    }
}

@Composable
private fun QuizParticipationScreen(
    modifier: Modifier,
    viewModel: QuizParticipationViewModel,
    onNavigateUp: () -> Unit
) {
    val exerciseDataState = viewModel.quizExerciseDataState.collectAsState().value
    val isWaitingForQuizStart by viewModel.waitingForQuizStart.collectAsState(initial = true)

    var displaySubmitDialog: Boolean by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = exerciseDataState.bind { it.title.orEmpty() }.orElse("")) },
                navigationIcon = {
                    // TODO: Add close dialog to prevent accidental back navigation
                    IconButton(onClick = { onNavigateUp() }) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                },
                actions = {
                    if (!isWaitingForQuizStart) {
                        OutlinedButton(onClick = { displaySubmitDialog = true }) {
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
                .padding(padding), viewModel = viewModel
        )

        if (displaySubmitDialog) {
            AlertDialog(
                onDismissRequest = { displaySubmitDialog = false },
                title = { Text(text = stringResource(id = R.string.quiz_participation_submit_dialog_title)) },
                text = { Text(text = stringResource(id = R.string.quiz_participation_submit_dialog_message)) },
                confirmButton = {
                    TextButton(onClick = { /*TODO*/ }) {
                        Text(text = stringResource(id = R.string.quiz_participation_submit_dialog_positive))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { displaySubmitDialog = false }) {
                        Text(text = stringResource(id = R.string.quiz_participation_submit_dialog_negative))
                    }
                }
            )
        }
    }
}