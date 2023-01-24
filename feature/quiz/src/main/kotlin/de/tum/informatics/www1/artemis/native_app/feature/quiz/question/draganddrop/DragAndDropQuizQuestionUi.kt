package de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizQuestionData
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.QuizQuestionBodyText
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.body.DragAndDropQuizQuestionBody
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.header.QuizQuestionHeader
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.header.toQuizQuestionHeaderType

/**
 * Draw the header and the body of the drag and drop quiz question
 */
@Composable
internal fun DragAndDropQuizQuestionUi(
    modifier: Modifier,
    questionIndex: Int,
    question: DragAndDropQuizQuestion,
    data: QuizQuestionData.DragAndDropData,
    serverUrl: String,
    authToken: String,
    onRequestDisplayHint: () -> Unit,
) {
    Column(modifier = modifier) {
        QuizQuestionHeader(
            modifier = Modifier.fillMaxWidth(),
            question = question,
            questionIndex = questionIndex,
            type = data.toQuizQuestionHeaderType(
                onRequestDisplayHint = onRequestDisplayHint
            )
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuizQuestionBodyText(
                modifier = Modifier.fillMaxWidth(),
                question = question
            )

            DragAndDropQuizQuestionBody(
                question,
                data,
                authToken,
                serverUrl,
            )
        }
    }
}
