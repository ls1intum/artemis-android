package de.tum.informatics.www1.artemis.native_app.feature.quiz.question.header

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.QuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExercisePointsDecimalFormat
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.resultMedium
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.resultSuccess
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizQuestionData
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.HelpText

private val QuizQuestionPointsDecimalFormat = ExercisePointsDecimalFormat

internal sealed interface QuizQuestionHeaderType {
    data class NoResult(
        val canDisplayQuestionHint: Boolean,
        val onRequestDisplayHint: () -> Unit,
    ) : QuizQuestionHeaderType

    data class Result(
        val achievedPoints: Double,
        val hint: String?
    ) : QuizQuestionHeaderType
}

/**
 * Helper method to fill in values from a question directly.
 */
@Composable
internal fun QuizQuestionHeader(
    modifier: Modifier,
    question: QuizQuestion,
    questionIndex: Int,
    type: QuizQuestionHeaderType
) {
    QuizQuestionHeader(
        modifier = modifier,
        questionIndex = questionIndex,
        title = question.title.orEmpty(),
        points = question.points ?: 1,
        type = type,
    )
}

@Composable
internal fun QuizQuestionHeader(
    modifier: Modifier,
    questionIndex: Int,
    title: String,
    points: Int = 1,
    type: QuizQuestionHeaderType
) {
    Column(modifier = modifier) {
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

            if (type is QuizQuestionHeaderType.NoResult) {
                if (type.canDisplayQuestionHint) {
                    Button(onClick = type.onRequestDisplayHint) {
                        Icon(imageVector = Icons.Default.Help, contentDescription = null)

                        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))

                        Text(text = stringResource(id = R.string.quiz_participation_question_header_hint_button))
                    }
                } else {
                    Box(modifier = Modifier.height(ButtonDefaults.MinHeight))
                }
            }

            ScoreInfo(modifier = Modifier, type = type, points = points)
        }
    }

    if (type is QuizQuestionHeaderType.Result && type.hint != null) {
        HelpText(modifier = Modifier.fillMaxWidth(), help = type.hint)
    }
}

@Composable
private fun ScoreInfo(
    modifier: Modifier,
    points: Int,
    type: QuizQuestionHeaderType
) {
    val (scoreText, scoreTextColor) = when (type) {
        is QuizQuestionHeaderType.NoResult -> {
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

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = scoreText,
            color = scoreTextColor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )

        // TODO: Wait for https://github.com/ls1intum/Artemis/issues/6139
//        if (type is QuizQuestionHeaderType.Result) {
//            IconButton(onClick = { displayGradingInfo = true }) {
//                Icon(imageVector = Icons.Default.QuestionMark, contentDescription = null)
//            }
//        }
    }
}

internal fun <T : QuizQuestion> QuizQuestionData<T>.toQuizQuestionHeaderType(
    onRequestDisplayHint: () -> Unit
): QuizQuestionHeaderType =
    if (this is QuizQuestionData.ResultData) {
        QuizQuestionHeaderType.Result(
            achievedPoints = achievedPoints,
            hint = question.hint
        )
    } else QuizQuestionHeaderType.NoResult(
        canDisplayQuestionHint = question.hint != null,
        onRequestDisplayHint = onRequestDisplayHint
    )

@Preview
@Composable
private fun QuizQuestionHeaderPreview() {
    Surface {
        QuizQuestionHeader(
            modifier = Modifier.fillMaxWidth(),
            questionIndex = 0,
            title = "My multiple choice question",
            points = 1,
            type = QuizQuestionHeaderType.NoResult(
                canDisplayQuestionHint = true,
                onRequestDisplayHint = {}
            )
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
            points = 1,
            type = QuizQuestionHeaderType.NoResult(
                canDisplayQuestionHint = true,
                onRequestDisplayHint = {}
            )
        )
    }
}