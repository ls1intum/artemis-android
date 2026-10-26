package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import android.app.Activity
import android.content.pm.ActivityInfo
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LectureFullscreenPageTest {
    @Test
    fun fullscreenUsesLandscapeAndRestoresRequestedPortraitOnExit() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val controller = LectureFullscreenController(activity, activity.requestedOrientation)
        controller.setFullscreen(true)
        assertEquals(
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE,
            activity.requestedOrientation,
        )
        controller.setFullscreen(false)
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, activity.requestedOrientation)
    }

    @Test
    fun backLeavesFullscreenBeforeLeavingThePageAndDisposeRestoresOrientation() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val controller =
            LectureFullscreenController(activity, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        var exited = 0
        var left = 0
        controller.setFullscreen(true)
        controller.back(
            {
                exited++
                controller.setFullscreen(false)
            },
            { left++ },
        )
        assertEquals(1, exited)
        assertEquals(0, left)
        controller.back({ exited++ }, { left++ })
        assertEquals(1, left)
        controller.setFullscreen(true)
        controller.close()
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, activity.requestedOrientation)
    }
}
