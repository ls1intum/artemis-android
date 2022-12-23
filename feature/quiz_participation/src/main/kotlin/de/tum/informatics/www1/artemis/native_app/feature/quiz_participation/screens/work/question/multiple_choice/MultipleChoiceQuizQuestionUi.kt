package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work.question.multiple_choice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.MultipleChoiceQuizQuestion
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.R
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work.question.QuizQuestionBodyText
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work.question.QuizQuestionHeader
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work.question.QuizQuestionInstructionText

/**
 * @param optionSelectionMapping map the option id to its selection state. Default selection state is false
 */
@Composable
internal fun MultipleChoiceQuizQuestionUi(
    modifier: Modifier,
    questionIndex: Int,
    question: MultipleChoiceQuizQuestion,
    optionSelectionMapping: Map<Long, Boolean>,
    onRequestDisplayHint: () -> Unit,
    onRequestDisplayAnswerOptionHint: (MultipleChoiceQuizQuestion.AnswerOption) -> Unit,
    onRequestChangeAnswerOptionSelectionState: (MultipleChoiceQuizQuestion.AnswerOption, isSelected: Boolean) -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        QuizQuestionHeader(
            modifier = Modifier.fillMaxWidth(),
            questionIndex = questionIndex,
            onRequestDisplayHint = onRequestDisplayHint,
            question = question
        )

        QuizQuestionBodyText(
            modifier = Modifier.fillMaxWidth(),
            question = question
        )

        QuizQuestionInstructionText(
            modifier = Modifier.fillMaxWidth(),
            instructionText = stringResource(
                id = if (question.singleChoice) {
                    R.string.quiz_participation_single_choice_instruction
                } else {
                    R.string.quiz_participation_multiple_choice_instruction
                }
            )
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            question.answerOptions.forEach { option ->
                val isCurrentlySelected = optionSelectionMapping[option.id] ?: false

                ChoiceItem(
                    modifier = Modifier.fillMaxWidth(),
                    text = option.text.orEmpty(),
                    hasHint = option.hint != null,
                    isSelected = isCurrentlySelected,
                    isSingleChoice = question.singleChoice,
                    onRequestDisplayHint = { onRequestDisplayAnswerOptionHint(option) },
                    onRequestSelect = { isSelected ->
                        onRequestChangeAnswerOptionSelectionState(option, isSelected)
                    }
                )
            }
        }
    }
}

