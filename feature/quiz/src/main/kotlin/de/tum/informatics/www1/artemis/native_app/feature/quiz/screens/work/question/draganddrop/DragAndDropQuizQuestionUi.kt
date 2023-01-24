package de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.draganddrop

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizQuestionData
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.QuizQuestionBodyText
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.QuizQuestionHeader
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.draganddrop.body.DragAndDropQuizQuestionBody
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.toQuizQuestionHeaderType

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
            questionIndex = questionIndex,
            onRequestDisplayHint = onRequestDisplayHint,
            question = question,
            type = data.toQuizQuestionHeaderType()
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
