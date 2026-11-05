package de.tum.informatics.www1.artemis.native_app.core.model

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.MultipleChoiceQuizQuestion
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.experimental.categories.Category

@Category(UnitTest::class)
class QuizCorrectnessVisibilityTest {
    @Test fun `omitted correctness remains unknown instead of wrong`() {
        val option = Json.decodeFromString<MultipleChoiceQuizQuestion.AnswerOption>("""{"id":3,"text":"Withheld"}""")
        assertNull(option.isCorrect)
    }

    @Test fun `published false and true correctness remain distinct`() {
        assertEquals(false, Json.decodeFromString<MultipleChoiceQuizQuestion.AnswerOption>("""{"id":3,"isCorrect":false}""").isCorrect)
        assertEquals(true, Json.decodeFromString<MultipleChoiceQuizQuestion.AnswerOption>("""{"id":4,"isCorrect":true}""").isCorrect)
    }
}
