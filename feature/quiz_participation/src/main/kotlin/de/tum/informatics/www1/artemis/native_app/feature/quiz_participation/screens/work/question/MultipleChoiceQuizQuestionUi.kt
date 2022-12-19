package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work.question

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.MultipleChoiceQuizQuestion

@Composable
internal fun MultipleChoiceQuizQuestionUi(
    modifier: Modifier,
    questionIndex: Int,
    question: MultipleChoiceQuizQuestion,
    onRequestDisplayHint: () -> Unit
) {
    Column(modifier = modifier) {
        QuizQuestionHeader(
            modifier = Modifier.fillMaxWidth(),
            questionIndex = questionIndex,
            title = question.title.orEmpty(),
            hasHint = question.hint != null,
            onRequestDisplayHint = onRequestDisplayHint
        )
    }
}
