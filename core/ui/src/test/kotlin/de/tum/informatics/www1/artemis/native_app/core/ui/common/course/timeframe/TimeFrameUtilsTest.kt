package de.tum.informatics.www1.artemis.native_app.core.ui.common.course.timeframe

import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.timeframe.TimeFrameUtils.defaultExpandedTimeFrames
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeFrameUtilsTest {

    @Test
    fun `test GIVEN many current and dueSoon TimeFrames WHEN calling defaultExpandedTimeFrames THEN only return current and dueSoon`() {
        val timeFrames = listOf(
            TimeFrame.Current(items(5)),
            TimeFrame.DueSoon(items(5)),
            TimeFrame.Past(items(5))
        )

        val result = defaultExpandedTimeFrames(timeFrames)

        assertEquals(2, result.size)
        assertTrue(result.contains(TimeFrame.Current::class.java))
        assertTrue(result.contains(TimeFrame.DueSoon::class.java))
    }

    @Test
    fun `test GIVEN few current and dueSoon but many future WHEN calling defaultExpandedTimeFrames THEN also future is open`() {
        val timeFrames = listOf(
            TimeFrame.Current(items(2)),
            TimeFrame.Future(items(13)),
            TimeFrame.Past(items(5))
        )

        val result = defaultExpandedTimeFrames(timeFrames)

        assertEquals(3, result.size)
        assertTrue(result.contains(TimeFrame.Current::class.java))
        assertTrue(result.contains(TimeFrame.DueSoon::class.java))
        assertTrue(result.contains(TimeFrame.Future::class.java))
    }

    @Test
    fun `test GIVEN few current, dueSoon, and future TimeFrames WHEN calling defaultExpandedTimeFrames THEN also past is open`() {
        val timeFrames = listOf(
            TimeFrame.Current(items(2)),
            TimeFrame.DueSoon(items(2)),
            TimeFrame.Future(items(2)),
            TimeFrame.Past(items(5))
        )

        val result = defaultExpandedTimeFrames(timeFrames)

        assertEquals(4, result.size)
        assertTrue(result.contains(TimeFrame.Current::class.java))
        assertTrue(result.contains(TimeFrame.DueSoon::class.java))
        assertTrue(result.contains(TimeFrame.Future::class.java))
        assertTrue(result.contains(TimeFrame.Past::class.java))
    }

    @Test
    fun `test GIVEN only noDate entries WHEN calling defaultExpandedTimeFrames THEN return noDate`() {
        val timeFrames = listOf(
            TimeFrame.NoDate(items(5))
        )

        val result = defaultExpandedTimeFrames(timeFrames)
        assertTrue(result.contains(TimeFrame.NoDate::class.java))
    }


    private fun items(count: Int): List<Unit> {
        return List(count) { Unit }
    }
}