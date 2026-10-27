package de.tum.informatics.www1.artemis.native_app.feature.quiz.training

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onNodeWithText
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.MultipleChoiceQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.header.QuizQuestionHeader
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.header.QuizQuestionHeaderType
import org.junit.Test
import org.junit.experimental.categories.Category

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@Category(UnitTest::class)
class QuizTrainingHeaderTest : BaseComposeTest() {
    @Test
    fun `missing question maximum does not invent one point`() {
        composeTestRule.setContent {
            QuizQuestionHeader(
                Modifier,
                MultipleChoiceQuizQuestion(id = 1, title = "Training question", points = null),
                0,
                QuizQuestionHeaderType.NoResult(false, {}),
            )
        }
        composeTestRule.onNodeWithText("1P").assertDoesNotExist()
    }
}
