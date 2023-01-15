package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work.question

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.QuizQuestion
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.R

@Composable
internal fun QuizQuestionHeader(
    modifier: Modifier,
    question: QuizQuestion,
    questionIndex: Int,
    onRequestDisplayHint: () -> Unit
) {
    QuizQuestionHeader(
        modifier = modifier,
        questionIndex = questionIndex,
        title = question.title.orEmpty(),
        hasHint = question.hint != null,
        onRequestDisplayHint = onRequestDisplayHint,
        points = question.points
    )
}

@Composable
internal fun QuizQuestionHeader(
    modifier: Modifier,
    questionIndex: Int,
    title: String,
    hasHint: Boolean,
    points: Int?,
    onRequestDisplayHint: () -> Unit
) {
    Row(
        modifier = modifier,
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

        if (points != null) {
            Text(
                modifier = Modifier,
                text = stringResource(
                    id = R.string.quiz_participation_question_header_points,
                    points
                ),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

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
            points = 1
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
            points = 1
        )
    }
}