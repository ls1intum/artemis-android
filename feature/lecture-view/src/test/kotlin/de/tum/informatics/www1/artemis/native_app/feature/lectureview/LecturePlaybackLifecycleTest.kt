package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import org.junit.Assert.*
import org.junit.Test

@org.junit.experimental.categories.Category(de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest::class)
class LecturePlaybackLifecycleTest {
    @Test fun interruptionSavesPositionBeforePausing() {
        val actions = mutableListOf<String>()
        val lifecycle = LecturePlaybackLifecycle({ 4200L }, { actions += "save:$it" }, { actions += "pause" }, { actions += "release" })
        lifecycle.pause()
        assertEquals(listOf("save:4200", "pause"), actions)
    }
    @Test fun dismissalReleasesOnceAndLateUpdatesCannotReadReleasedPlayer() {
        var released = false
        var reads = 0
        val actions = mutableListOf<String>()
        val lifecycle = LecturePlaybackLifecycle({ check(!released); reads++; 1200L }, { actions += "save:$it" }, { actions += "pause" }, { released = true; actions += "release" })
        lifecycle.close()
        lifecycle.close()
        lifecycle.save()
        lifecycle.pause()
        assertEquals(listOf("save:1200", "pause", "release"), actions)
        assertEquals(1, reads)
        assertTrue(released)
    }
}
