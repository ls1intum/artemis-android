package de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.QuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExercisePointsDecimalFormat
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.resultMedium
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.resultSuccess
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizQuestionData

private val QuizQuestionPointsDecimalFormat = ExercisePointsDecimalFormat

internal sealed interface QuizQuestionHeaderType {
    object NoResult : QuizQuestionHeaderType

    data class Result(val achievedPoints: Double) : QuizQuestionHeaderType
}

@Composable
internal fun QuizQuestionHeader(
    modifier: Modifier,
    question: QuizQuestion,
    questionIndex: Int,
    type: QuizQuestionHeaderType,
    onRequestDisplayHint: () -> Unit
) {
    QuizQuestionHeader(
        modifier = modifier,
        questionIndex = questionIndex,
        title = question.title.orEmpty(),
        hasHint = question.hint != null,
        onRequestDisplayHint = onRequestDisplayHint,
        points = question.points ?: 1,
        type = type,
    )
}

@Composable
internal fun QuizQuestionHeader(
    modifier: Modifier,
    questionIndex: Int,
    title: String,
    hasHint: Boolean,
    points: Int = 1,
    type: QuizQuestionHeaderType,
    onRequestDisplayHint: () -> Unit
) {
    var displayScoreInRow by remember { mutableStateOf(false) }

    val (scoreText, scoreTextColor) = when (type) {
        QuizQuestionHeaderType.NoResult -> {
            stringResource(
                id = R.string.quiz_participation_question_header_points,
                points
            ) to LocalTextStyle.current.color
        }
        is QuizQuestionHeaderType.Result -> {
            val maxPointsFormatted = remember(points) {
                QuizQuestionPointsDecimalFormat.format(points)
            }

            val achievedPointsFormatted = remember(type.achievedPoints) {
                QuizQuestionPointsDecimalFormat.format(type.achievedPoints)
            }

            val isCorrect = type.achievedPoints == points.toDouble()

            stringResource(
                id = R.string.quiz_result_question_header_points,
                achievedPointsFormatted,
                maxPointsFormatted
            ) to if (isCorrect) resultSuccess else resultMedium
        }
    }


    val score = @Composable {
        Text(
            text = scoreText,
            color = scoreTextColor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            onTextLayout = { layoutResult ->
                if (layoutResult.hasVisualOverflow) {
                    displayScoreInRow = true
                }
            }
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(
                    id = R.string.quiz_participation_question_header_title,
                    questionIndex + 1,
                    title
                ),
                style = MaterialTheme.typography.titleLarge
            )

            if (hasHint) {
                Button(onClick = onRequestDisplayHint) {
                    Icon(imageVector = Icons.Default.Help, contentDescription = null)

                    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))

                    Text(text = stringResource(id = R.string.quiz_participation_question_header_hint_button))
                }
            } else {
                Box(modifier = Modifier.height(ButtonDefaults.MinHeight))
            }

            if (!displayScoreInRow) {
                score()
            }
        }

        if (displayScoreInRow) {
            score()
        }
    }
}

internal fun <T : QuizQuestion> QuizQuestionData<T>.toQuizQuestionHeaderType(): QuizQuestionHeaderType =
    if (this is QuizQuestionData.ResultData) {
        QuizQuestionHeaderType.Result(achievedPoints = achievedPoints)
    } else QuizQuestionHeaderType.NoResult

@Preview
@Composable
private fun QuizQuestionHeaderPreview() {
    Surface {
        QuizQuestionHeader(
            modifier = Modifier.fillMaxWidth(),
            questionIndex = 0,
            title = "My multiple choice question",
            hasHint = true,
            onRequestDisplayHint = {},
            points = 1,
            type = QuizQuestionHeaderType.NoResult
        )
    }
}

@Preview
@Composable
private fun QuizQuestionHeaderPreviewMultiline() {
    Surface {
        QuizQuestionHeader(
            modifier = Modifier.fillMaxWidth(),
            questionIndex = 0,
            title = "My multiple choice question with a very long title that will stretch over multiple lines",
            hasHint = true,
            onRequestDisplayHint = {},
            points = 1,
            type = QuizQuestionHeaderType.NoResult
        )
    }
}