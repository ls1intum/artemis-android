package de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.ExerciseColors
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R

@Composable internal fun QuizReviewOutcome.label(): String = stringResource(when (this) {
    QuizReviewOutcome.CORRECT -> R.string.quiz_result_mc_correct
    QuizReviewOutcome.INCORRECT -> R.string.quiz_result_mc_wrong
    QuizReviewOutcome.WITHHELD -> R.string.quiz_result_correctness_withheld
    QuizReviewOutcome.INVALID -> R.string.quiz_review_invalid
})
@Composable internal fun QuizReviewOutcome.color(): Color = when (this) {
    QuizReviewOutcome.CORRECT -> ExerciseColors.Result.success
    QuizReviewOutcome.INCORRECT -> ExerciseColors.Result.medium
    QuizReviewOutcome.WITHHELD, QuizReviewOutcome.INVALID -> MaterialTheme.colorScheme.outline
}
