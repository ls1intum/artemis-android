package de.tum.informatics.www1.artemis.native_app.feature.push

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CurrentActivityListener : ActivityLifecycleCallbacks {

    private val _currentActivity = MutableStateFlow<Activity?>(null)
    val currentActivity: StateFlow<Activity?> = _currentActivity

    override fun onActivityResumed(activity: Activity) {
        _currentActivity.value = activity
    }

    override fun onActivityPaused(activity: Activity) {
        if (_currentActivity.value == activity) {
            _currentActivity.value = null
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }
}