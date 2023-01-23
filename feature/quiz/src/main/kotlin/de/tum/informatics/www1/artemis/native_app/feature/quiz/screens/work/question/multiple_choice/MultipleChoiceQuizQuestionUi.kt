package de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.multiple_choice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.MultipleChoiceQuizQuestion
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizQuestionData
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.QuizQuestionBodyText
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.QuizQuestionHeader
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.QuizQuestionInstructionText

@Composable
internal fun MultipleChoiceQuizQuestionUi(
    modifier: Modifier,
    questionIndex: Int,
    onRequestDisplayHint: () -> Unit,
    onRequestDisplayAnswerOptionHint: (MultipleChoiceQuizQuestion.AnswerOption) -> Unit,
    data: QuizQuestionData.MultipleChoiceData
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        QuizQuestionHeader(
            modifier = Modifier.fillMaxWidth(),
            questionIndex = questionIndex,
            onRequestDisplayHint = onRequestDisplayHint,
            question = data.question
        )

        QuizQuestionBodyText(
            modifier = Modifier.fillMaxWidth(),
            question = data.question
        )

        QuizQuestionInstructionText(
            modifier = Modifier.fillMaxWidth(),
            instructionText = stringResource(
                id = if (data.question.singleChoice) {
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
            data.question.answerOptions.forEach { option ->
                val isCurrentlySelected = data.optionSelectionMapping[option.id] ?: false

                ChoiceItem(
                    modifier = Modifier.fillMaxWidth(),
                    text = option.text.orEmpty(),
                    hasHint = option.hint != null,
                    isSelected = isCurrentlySelected,
                    isSingleChoice = data.question.singleChoice,
                    type = when (data) {
                        is QuizQuestionData.MultipleChoiceData.Editable -> ChoiceItemType.Editable(
                            onRequestSelect = { isSelected ->
                                data.onRequestChangeAnswerOptionSelectionState(
                                    option.id,
                                    isSelected
                                )
                            }
                        )
                        is QuizQuestionData.MultipleChoiceData.Result -> ChoiceItemType.ViewResult
                    },
                    onRequestDisplayHint = { onRequestDisplayAnswerOptionHint(option) }
                )
            }
        }
    }
}

