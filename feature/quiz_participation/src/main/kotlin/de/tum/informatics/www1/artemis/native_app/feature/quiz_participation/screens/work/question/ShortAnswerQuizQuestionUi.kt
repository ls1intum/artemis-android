package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work.question

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.ShortAnswerQuizQuestion

private val spotRegExpo = "\\[-spot\\s*([0-9]+)]".toRegex()

@Composable
internal fun ShortAnswerQuizQuestionUi(
    modifier: Modifier,
    questionIndex: Int,
    question: ShortAnswerQuizQuestion,
    solutionTexts: Map<Int, String>,
    onUpdateSolutionText: (spotId: Int, newSolutionText: String) -> Unit,
    onRequestDisplayHint: () -> Unit
) {
    val annotatedString = remember(question.text) {
        shortAnswerBuildAnnotatedString(question.text.orEmpty())
    }

    val inlineContentMap = remember(question.spots, solutionTexts) {
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
                    BasicTextField(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            .padding(horizontal = 2.dp),
                        value = solutionTexts[spotNr].orEmpty(),
                        onValueChange = { newText ->
                            onUpdateSolutionText(spotNr, newText)
                        },
                        maxLines = 1
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
            title = question.title.orEmpty(),
            hasHint = question.hint != null,
            onRequestDisplayHint = onRequestDisplayHint
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