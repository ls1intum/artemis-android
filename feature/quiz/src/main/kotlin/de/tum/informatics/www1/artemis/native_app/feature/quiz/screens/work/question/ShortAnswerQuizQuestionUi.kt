package de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question

import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizQuestionData

private val spotRegExpo = "\\[-spot\\s*([0-9]+)]".toRegex()

@Composable
internal fun ShortAnswerQuizQuestionUi(
    modifier: Modifier,
    questionIndex: Int,
    data: QuizQuestionData.ShortAnswerData,
    onRequestDisplayHint: () -> Unit
) {
    val question = data.question

    val annotatedString = remember(question.text) {
        shortAnswerBuildAnnotatedString(question.text.orEmpty())
    }

    val areTextFieldsEnabled = when (data) {
        is QuizQuestionData.ShortAnswerData.Editable -> true
        is QuizQuestionData.ShortAnswerData.Result -> false
    }

    val inlineContentMap = remember(question.spots, data.solutionTexts) {
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

                    BasicTextField(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            .padding(horizontal = 2.dp),
                        value = data.solutionTexts[spotNr].orEmpty(),
                        enabled = areTextFieldsEnabled,
                        onValueChange = { newText ->
                            if (data is QuizQuestionData.ShortAnswerData.Editable) {
                                data.onUpdateSolutionText(spotNr, newText)
                            }
                        },
                        maxLines = 1,
                        textStyle = LocalTextStyle.current.copy(color = textColor),
                        cursorBrush = SolidColor(textColor)
                    )
                }
            )

            key to value
        }
    }

    Column(modifier = modifier) {
        QuizQuestionHeader(
            modifier = Modifier.fillMaxWidth(),
            questionIndex = questionIndex,
            onRequestDisplayHint = onRequestDisplayHint,
            question = question,
            type = data.toQuizQuestionHeaderType()
        )

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = annotatedString,
            inlineContent = inlineContentMap
        )
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