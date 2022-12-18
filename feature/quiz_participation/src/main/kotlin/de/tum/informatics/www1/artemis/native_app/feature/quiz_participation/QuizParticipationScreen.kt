package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavController.navigateToQuizParticipation(
    exerciseId: Long,
    quizType: QuizType
) {
    navigate("quiz-participation?$exerciseId&$quizType")
}

fun NavGraphBuilder.quizParticipation(onLeaveQuiz: () -> Unit) {
    composable(
        "quiz-participation?{exerciseId}&{quizType}",
        arguments = listOf(
            navArgument("exerciseId") {
                type = NavType.LongType
            },
            navArgument("quizType") {
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        val exerciseId = backStackEntry.arguments?.getLong("exerciseId")
        val quizTypeString = backStackEntry.arguments?.getString("quizType")

        checkNotNull(exerciseId)
        checkNotNull(quizTypeString)

        val quizType: QuizType = QuizType.valueOf(quizTypeString)

        val viewModel: QuizParticipationViewModel =
            koinViewModel { parametersOf(exerciseId, quizType) }

        QuizParticipationScreen(modifier = Modifier.fillMaxSize(), viewModel = viewModel)
    }
}

@Composable
private fun QuizParticipationScreen(modifier: Modifier, viewModel: QuizParticipationViewModel) {

}