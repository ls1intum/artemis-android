package de.tum.informatics.www1.artemis.native_app.feature.quiz

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result.quizResultFraction
import org.junit.Assert.*
import org.junit.Test
import org.junit.experimental.categories.Category

@Category(UnitTest::class)
class QuizScoreVisibilityTest {
    @Test fun `absolute quiz points yield a fraction only with a known positive maximum`() {
        assertEquals(0.75, quizResultFraction(7.5, 10.0)!!, 0.0)
        assertEquals(0.0, quizResultFraction(0.0, 10.0)!!, 0.0)
        assertNull(quizResultFraction(null, 10.0))
        assertNull(quizResultFraction(0.0, 0.0))
        assertNull(quizResultFraction(1.0, Double.NaN))
    }
}
