package de.tum.informatics.www1.artemis.native_app.core.common

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import kotlinx.coroutines.flow.StateFlow

interface CurrentActivityListener : ActivityLifecycleCallbacks {

    val currentActivity: StateFlow<Activity?>
}
