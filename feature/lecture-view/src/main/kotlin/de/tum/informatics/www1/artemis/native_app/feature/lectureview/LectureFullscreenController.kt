package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/** Window/orientation ownership only. The existing player is never replaced for fullscreen. */
internal class LectureFullscreenController(private val activity: Activity, private val originalOrientation: Int) {
    private var fullscreen = false
    fun setFullscreen(value: Boolean) {
        fullscreen = value
        val bars=WindowCompat.getInsetsController(activity.window,activity.window.decorView)
        if(value) {
            activity.requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            bars.systemBarsBehavior=WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            bars.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            activity.requestedOrientation=originalOrientation
            bars.show(WindowInsetsCompat.Type.systemBars())
        }
    }
    fun back(exitFullscreen: () -> Unit, leavePage: () -> Unit) { if(fullscreen)exitFullscreen() else leavePage() }
    fun close() { setFullscreen(false) }
}
