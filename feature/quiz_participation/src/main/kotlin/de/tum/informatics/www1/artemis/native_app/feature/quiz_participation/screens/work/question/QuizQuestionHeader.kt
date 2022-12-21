package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work.question

import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.R

@Composable
internal fun QuizQuestionHeader(
    modifier: Modifier,
    questionIndex: Int,
    title: String,
    hasHint: Boolean,
    onRequestDisplayHint: () -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
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
            onRequestDisplayHint = {}
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
            onRequestDisplayHint = {}
        )
    }
}