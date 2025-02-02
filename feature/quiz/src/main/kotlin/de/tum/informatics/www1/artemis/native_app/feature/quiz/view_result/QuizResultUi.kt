package de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.QuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExercisePointsDecimalFormat
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.ExerciseColors
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizQuestionData
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.QuizQuestionBody
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.QuizOverviewRow
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@Composable
internal fun QuizResultUi(
    modifier: Modifier,
    quizTitle: String,
    achievedPoints: Double,
    maxPoints: Int,
    quizQuestions: List<QuizQuestion>,
    quizQuestionsWithData: List<QuizQuestionData<*>>,
) {
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    val selectedQuestionIndex by remember {
        derivedStateOf {
            (lazyListState.firstVisibleItemIndex - 1).coerceAtLeast(0)
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        QuizResultHeader(
            modifier = Modifier.fillMaxWidth(),
            quizQuestions = quizQuestions,
            selectedQuestionIndex = selectedQuestionIndex,
            onClickQuestion = {
                scope.launch {
                    lazyListState.scrollToItem(it + 1)
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .widthIn(max = 800.dp)
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            item {
                QuizQuestionListHeader(
                    modifier = Modifier.fillMaxWidth(),
                    quizTitle = quizTitle,
                    achievedPoints = achievedPoints,
                    maxPoints = maxPoints
                )
            }
            itemsIndexed(quizQuestionsWithData) { index, questionWithData ->
                if (questionWithData is QuizQuestionData.ResultData) {
                    val isCorrect =
                        questionWithData.question.points?.toDouble() == questionWithData.achievedPoints

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (isCorrect) ExerciseColors.Result.success else ExerciseColors.Result.medium
                            )
                            .padding(8.dp)
                    ) {
                        QuizQuestionBody(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center),
                            questionIndex = index,
                            quizQuestionData = questionWithData,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizResultHeader(
    modifier: Modifier,
    quizQuestions: List<QuizQuestion>,
    selectedQuestionIndex: Int,
    onClickQuestion: (questionIndex: Int) -> Unit
) {
    QuizOverviewRow(
        modifier = modifier,
        questions = quizQuestions,
        selectedQuestionIndex = selectedQuestionIndex,
        onClickQuestion = onClickQuestion
    )
}

@Composable
private fun QuizQuestionListHeader(
    modifier: Modifier,
    quizTitle: String,
    achievedPoints: Double,
    maxPoints: Int
) {
    Column(modifier) {
        Text(
            text = quizTitle,
            style = MaterialTheme.typography.headlineLarge
        )

        QuizScoreResultOverview(
            modifier = Modifier.fillMaxWidth(),
            achievedPoints = achievedPoints,
            maxPoints = maxPoints
        )
    }
}

@Composable
private fun QuizScoreResultOverview(modifier: Modifier, achievedPoints: Double, maxPoints: Int) {
    val achievedPointsFormatted = remember(achievedPoints) {
        ExercisePointsDecimalFormat.format(achievedPoints)
    }

    val maxPointsFormatted = remember(maxPoints) {
        ExercisePointsDecimalFormat.format(maxPoints)
    }

    val percentFormatted = remember(achievedPoints, maxPoints) {
        val percent = achievedPoints.toFloat() / maxPoints.toFloat()
        DecimalFormat.getPercentInstance().format(percent)
    }

    Text(
        modifier = modifier,
        text = stringResource(
            id = R.string.quiz_result_score_result,
            achievedPointsFormatted,
            maxPointsFormatted,
            percentFormatted
        ),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold
    )
}