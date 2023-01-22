package de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExercisePointsDecimalFormat
import de.tum.informatics.www1.artemis.native_app.feature.quiz.QuizType
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.text.DecimalFormat
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R

fun NavController.navigateToQuizParticipation(
    courseId: Long,
    exerciseId: Long
) {
    navigate("view-quiz-result/$courseId/$exerciseId")
}

fun NavGraphBuilder.quizParticipation(onLeaveQuiz: () -> Unit) {
    composable(
        "view-quiz-result/{courseId}/{exerciseId}",
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


    }
}

@Composable
internal fun ViewQuizResultScreen(
    modifier: Modifier,
    courseId: Long,
    exerciseId: Long,
    quizType: QuizType.ViewableQuizType,
    onNavigateUp: () -> Unit
) {
    val viewModel: QuizResultViewModel = koinViewModel { parametersOf(exerciseId, quizType) }

    val quizQuestions by viewModel.questi

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Results") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::retryLoadExercise) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { padding ->
        BasicDataStateUi(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            dataState = ,
            loadingText = ,
            failureText = ,
            retryButtonText = ,
            onClickRetry = { /*TODO*/ }) {

        }

        Column(
            modifier =
        ) {
            QuizScoreResultOverview(
                modifier = Modifier.fillMaxWidth(),
                achievedPoints = ,
                maxPoints =
            )
        }
    }
}

@Composable
private fun QuizScoreResultOverview(modifier: Modifier, achievedPoints: Int, maxPoints: Int) {
    val achievedPointsFormatted = remember(achievedPoints) {
        ExercisePointsDecimalFormat.format(achievedPoints)
    }

    val maxPointsFormatted = remember(maxPoints) {
        ExercisePointsDecimalFormat.format(maxPoints)
    }

    val percentFormatted = remember(achievedPoints, maxPoints) {
        val percent = achievedPoints.toFloat() / maxPoints.toFloat()
        DecimalFormat.getPercentInstance().format(percent)
    }

    Text(
        modifier = modifier,
        text = stringResource(
            id = R.string.quiz_result_score_result,
            achievedPointsFormatted,
            maxPointsFormatted,
            percentFormatted
        ),
        style = MaterialTheme.typography.bodyMedium
    )
}