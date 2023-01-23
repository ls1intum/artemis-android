package de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.QuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.resultMedium
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.resultSuccess
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizQuestionData
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.QuizQuestionBody
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.QuizOverviewRow
import kotlinx.coroutines.launch

@Composable
internal fun QuizResultUi(
    modifier: Modifier,
    quizQuestions: List<QuizQuestion>,
    quizQuestionsWithData: List<QuizQuestionData<*>>,
    serverUrl: String,
    authToken: String
) {
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    val selectedQuestionIndex by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }

    Column(modifier = modifier) {
        QuizResultHeader(
            modifier = Modifier.fillMaxWidth(),
            quizQuestions = quizQuestions,
            selectedQuestionIndex = selectedQuestionIndex,
            onClickQuestion = {
                scope.launch {
                    lazyListState.scrollToItem(it)
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            itemsIndexed(quizQuestionsWithData) { index, questionWithData ->
                if (questionWithData is QuizQuestionData.ResultData) {
                    val isCorrect =
                        questionWithData.question.points?.toDouble() == questionWithData.achievedPoints

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (isCorrect) resultSuccess else resultMedium
                            )
                            .padding(8.dp)
                    ) {
                        QuizQuestionBody(
                            modifier = Modifier
                                .widthIn(max = 800.dp)
                                .heightIn(max = 800.dp)
                                .align(Alignment.Center),
                            questionIndex = index,
                            quizQuestionData = questionWithData,
                            serverUrl = serverUrl,
                            authToken = authToken
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun QuizResultHeader(
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