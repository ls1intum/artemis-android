package de.tum.informatics.www1.artemis.native_app.feature.quiz.question

import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result.shortAnswerReviewOutcome
import de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result.label
import de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result.color
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizQuestionData
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.header.QuizQuestionHeader
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.header.toQuizQuestionHeaderType

private val spotRegExpo = "\\[-spot\\s*([0-9]+)]".toRegex()

@Composable
internal fun ShortAnswerQuizQuestionUi(
    modifier: Modifier,
    questionIndex: Int,
    data: QuizQuestionData.ShortAnswerData,
    onRequestDisplayHint: () -> Unit
) {
    var isSampleSolutionDisplayed by remember { mutableStateOf(false) }

    val question = data.question

    val annotatedString = remember(question.text) {
        shortAnswerBuildAnnotatedString(question.text.orEmpty())
    }

    val areTextFieldsEnabled = when (data) {
        is QuizQuestionData.ShortAnswerData.Editable -> true
        is QuizQuestionData.ShortAnswerData.Result -> false
    }

    val solutionTexts = if (isSampleSolutionDisplayed) {
        remember(data.question.correctMappings) {
            data.question.correctMappings
                .orEmpty()
                .mapNotNull { correctMapping ->
                    val spotNr = correctMapping.spot?.spotNr ?: return@mapNotNull null
                    spotNr to correctMapping.solution?.text.orEmpty()
                }
                .toMap()
        }
    } else data.solutionTexts

    val inlineContentMap = remember(question, solutionTexts, data, isSampleSolutionDisplayed) {
        val maxSpotNr = question.spots.maxOfOrNull { it.spotNr ?: 0 } ?: 0
        question.spots.associate { spot ->
            val spotNr = spot.spotNr ?: 0
            val key = spotNr.toString()
            val value = InlineTextContent(
                placeholder = Placeholder(
                    width = (spot.width ?: 10).em,
                    height = 22.sp,
                    PlaceholderVerticalAlign.Center
                ),
                children = {
                    val textColor = if (isSystemInDarkTheme()) Color.White else Color.Black
                    val reviewOutcome = (data as? QuizQuestionData.ShortAnswerData.Result)?.let { shortAnswerReviewOutcome(question, spot, it.correctness[spotNr]) }
                    val outcomeLabel = if (isSampleSolutionDisplayed) stringResource(R.string.quiz_review_sample) else reviewOutcome?.label()
                    val outline = if (isSampleSolutionDisplayed) MaterialTheme.colorScheme.outline else reviewOutcome?.color() ?: MaterialTheme.colorScheme.outline
                    val imeAction = if (spotNr == maxSpotNr) ImeAction.Done else ImeAction.Next

                    BasicTextField(
                        modifier = Modifier
                            .fillMaxSize()
                            .semantics { outcomeLabel?.let { stateDescription = it } }
                            .border(
                                width = 1.dp,
                                color = outline
                            )
                            .padding(horizontal = 2.dp),
                        value = solutionTexts[spotNr].orEmpty(),
                        enabled = areTextFieldsEnabled,
                        onValueChange = { newText ->
                            if (data is QuizQuestionData.ShortAnswerData.Editable) {
                                data.onUpdateSolutionText(spotNr, newText)
                            }
                        },
                        textStyle = LocalTextStyle.current.copy(color = textColor),
                        cursorBrush = SolidColor(textColor),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = imeAction)
                    )
                }
            )

            key to value
        }
    }

    Column(modifier = modifier) {
        QuizQuestionHeader(
            modifier = Modifier.fillMaxWidth(),
            question = question,
            questionIndex = questionIndex,
            type = data.toQuizQuestionHeaderType(
                onRequestDisplayHint
            )
        )

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = annotatedString,
            inlineContent = inlineContentMap
        )

        if (data is QuizQuestionData.ShortAnswerData.Result) {
            question.spots.forEach { spot ->
                val nr = spot.spotNr ?: return@forEach
                val status = shortAnswerReviewOutcome(question, spot, data.correctness[nr])
                Text(stringResource(R.string.quiz_review_blank_outcome, nr, status.label()), color = status.color(), style = MaterialTheme.typography.bodySmall)
            }
        }

        if (data is QuizQuestionData.ShortAnswerData.Result && !question.invalid && question.correctMappings != null) {
            val buttonText = if (isSampleSolutionDisplayed) {
                R.string.quiz_result_sa_hide_sample_solution
            } else R.string.quiz_result_sa_show_sample_solution

            OutlinedButton(
                modifier = Modifier.padding(vertical = 8.dp),
                onClick = { isSampleSolutionDisplayed = !isSampleSolutionDisplayed }
            ) {
                Text(text = stringResource(id = buttonText))
            }
        }
    }
}

private fun shortAnswerBuildAnnotatedString(questionText: String): AnnotatedString {
    return buildAnnotatedString {
        var matchResult = spotRegExpo.find(questionText)
        var prevPos = 0

        while (matchResult != null) {
            val prevText = questionText.substring(prevPos, matchResult.range.first)
            append(prevText)

            val inputFieldNumber = matchResult.groupValues[1]

            appendInlineContent(inputFieldNumber)

            prevPos = matchResult.range.last + 1
            matchResult = matchResult.next()
        }

        // match result is null
        if (prevPos < questionText.length) {
            append(questionText.substring(prevPos, questionText.length))
        }
    }
}