package de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.MultipleChoiceQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.QuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.ShortAnswerQuizQuestion
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R

@Composable
internal fun WorkOnQuizHeader(
    modifier: Modifier,
    questions: List<QuizQuestion>,
    selectedQuestionIndex: Int,
    overallPoints: Int,
    onChangeSelectionQuestionIndex: (questionIndex: Int) -> Unit
) {

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        QuizOverviewRow(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
            questions = questions,
            selectedQuestionIndex = selectedQuestionIndex,
            onClickQuestion = onChangeSelectionQuestionIndex
        )

        Text(
            modifier = Modifier.padding(8.dp),
            text = stringResource(
                id = R.string.quiz_participation_overall_score_points,
                overallPoints
            ),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Row that displays all questions as circle icons. The currently selected question is highlighted.
 */
@Composable
internal fun QuizOverviewRow(
    modifier: Modifier,
    questions: List<QuizQuestion>,
    selectedQuestionIndex: Int,
    onClickQuestion: (questionIndex: Int) -> Unit
) {
    val listState = rememberLazyListState()
    LaunchedEffect(key1 = selectedQuestionIndex) {
        listState.animateScrollToItem(selectedQuestionIndex)
    }

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        state = listState
    ) {
        itemsIndexed(questions, key = { _, q -> q.id }) { index, question ->
            val isSelected = index == selectedQuestionIndex

            QuizQuestionRowItem(
                modifier = Modifier.size(48.dp),
                question = question,
                isSelected = isSelected,
                hasAnswer = false,
                onClick = { onClickQuestion(index) }
            )
        }
    }
}

private val dragAndDropColor: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF296773) else Color(0xFF86ccd5)

private val multipleChoiceColor: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF9d8a1e) else Color(0xFFeee066)

private val shortAnswerColor: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF703e20) else Color(0xFFd8956c)

private val selectedBorderColor: Color
    @Composable get() = if (isSystemInDarkTheme()) Color.Green else Color.Green


@Composable
private fun QuizQuestionRowItem(
    modifier: Modifier,
    question: QuizQuestion,
    isSelected: Boolean,
    hasAnswer: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when (question) {
        is DragAndDropQuizQuestion -> dragAndDropColor
        is MultipleChoiceQuizQuestion -> multipleChoiceColor
        is ShortAnswerQuizQuestion -> shortAnswerColor
    }

    val shortText = when (question) {
        is DragAndDropQuizQuestion -> R.string.quiz_participation_drag_and_drop_short
        is MultipleChoiceQuizQuestion -> R.string.quiz_participation_multiple_choice_short
        is ShortAnswerQuizQuestion -> R.string.quiz_participation_short_answer_short
    }

    BoxWithConstraints(
        modifier = modifier.then(
            Modifier
                .background(
                    color = backgroundColor,
                    shape = CircleShape
                )
                .let {
                    if (isSelected) {
                        it.border(
                            width = 1.dp,
                            color = selectedBorderColor,
                            shape = CircleShape
                        )
                    } else it
                }
                .clip(CircleShape)
                .clickable(onClick = onClick)
        )
    ) {
        val contentColor = if (isSystemInDarkTheme()) {
            Color.White
        } else {
            Color.Black
        }

        val textSize = with(LocalDensity.current) {
            maxHeight.toPx().toSp() * 0.4f
        }

        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(id = shortText),
            fontSize = textSize,
            color = contentColor
        )

        if (hasAnswer) {
            Icon(
                modifier = Modifier.fillMaxSize(),
                imageVector = Icons.Default.Done,
                contentDescription = null,
                tint = contentColor
            )
        }
    }
}

private class PreviewQuestionProvider : PreviewParameterProvider<List<QuizQuestion>> {
    override val values: Sequence<List<QuizQuestion>> = sequenceOf(
        listOf(
            MultipleChoiceQuizQuestion(id = 1),
            ShortAnswerQuizQuestion(id = 2),
            DragAndDropQuizQuestion(id = 3)
        )
    )
}

@Preview
@Composable
private fun QuizHeaderPreview(
    @PreviewParameter(PreviewQuestionProvider::class) questions: List<QuizQuestion>
) {
    var selectedIndex by remember { mutableStateOf(0) }

    Surface {
        WorkOnQuizHeader(
            modifier = Modifier.fillMaxWidth(),
            questions = questions,
            selectedQuestionIndex = selectedIndex,
            overallPoints = 5
        ) { selectedIndex = it }
    }
}

@Preview
@Composable
private fun QuizOverviewRowPreview(
    @PreviewParameter(PreviewQuestionProvider::class) questions: List<QuizQuestion>
) {
    var selectedIndex by remember { mutableStateOf(0) }

    Surface {
        QuizOverviewRow(
            modifier = Modifier.fillMaxWidth(),
            questions = questions,
            selectedQuestionIndex = selectedIndex,
            onClickQuestion = { selectedIndex = it }
        )
    }
}