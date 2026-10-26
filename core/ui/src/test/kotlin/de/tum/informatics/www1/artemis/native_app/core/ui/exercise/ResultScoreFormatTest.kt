package de.tum.informatics.www1.artemis.native_app.core.ui.exercise

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.experimental.categories.Category
import java.util.Locale

@Category(UnitTest::class)
class ResultScoreFormatTest {
    @Test fun `overview and review share course precision without treating missing scores as zero`() {
        val score = formatExerciseResultScore(82.5f, 10f, 1, Locale.US)!!
        assertEquals("82.5%", score.percentage)
        assertEquals("8.3", score.points)
        assertEquals("10", score.maximum)
        assertNull(formatExerciseResultScore(null, 10f, 1, Locale.US))
        assertEquals("0", formatExerciseResultScore(0f, 10f, 1, Locale.US)?.points)
        assertEquals("11.3", formatExerciseResultScore(112.5f, 10f, 1, Locale.US)?.points)
        assertEquals("3.34", formatExerciseResultScore(33.35f, 10f, 2, Locale.US)?.points)
        assertEquals("8,3", formatExerciseResultScore(82.5f, 10f, 1, Locale.GERMAN)?.points)
        assertNull(formatExerciseResultScore(82.5f, null, 1, Locale.US)?.points)
    }
}
