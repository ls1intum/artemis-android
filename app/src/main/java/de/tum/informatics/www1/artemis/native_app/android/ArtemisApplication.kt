package de.tum.informatics.www1.artemis.native_app.android

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.memory.MemoryCache
import de.tum.informatics.www1.artemis.native_app.android.db.dbModule
import de.tum.informatics.www1.artemis.native_app.core.common.CurrentActivityListener
import de.tum.informatics.www1.artemis.native_app.core.data.dataModule
import de.tum.informatics.www1.artemis.native_app.core.datastore.datastoreModule
import de.tum.informatics.www1.artemis.native_app.core.device.deviceModule
import de.tum.informatics.www1.artemis.native_app.core.ui.uiModule
import de.tum.informatics.www1.artemis.native_app.core.websocket.websocketModule
import de.tum.informatics.www1.artemis.native_app.feature.course_registration.courseRegistrationModule
import de.tum.informatics.www1.artemis.native_app.feature.course_view.courseViewModule
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.dashboardModule
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.exerciseModule
import de.tum.informatics.www1.artemis.native_app.feature.lecture_view.lectureModule
import de.tum.informatics.www1.artemis.native_app.feature.login.loginModule
import de.tum.informatics.www1.artemis.native_app.feature.metis.communicationModule
import de.tum.informatics.www1.artemis.native_app.feature.push.ArtemisNotificationChannel
import de.tum.informatics.www1.artemis.native_app.feature.push.pushModule
import de.tum.informatics.www1.artemis.native_app.feature.quiz.quizParticipationModule
import de.tum.informatics.www1.artemis.native_app.feature.settings.settingsModule
import io.sentry.Sentry
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class ArtemisApplication : Application(), ImageLoaderFactory, CurrentActivityListener {

    override val currentActivity = MutableStateFlow<Activity?>(null)

    override fun onCreate() {
        super.onCreate()

        Sentry.init {
            it.dsn =
                if (BuildConfig.DEBUG) "" else "https://8b4d69ac628d4462995ee6178365541f@sentry.ase.in.tum.de/3"
        }

        startKoin {
            androidContext(this@ArtemisApplication)
            workManagerFactory()

            modules(
                dataModule,
                uiModule,
                datastoreModule,
                deviceModule,
                websocketModule,
                courseRegistrationModule,
                courseViewModule,
                dashboardModule,
                loginModule,
                exerciseModule,
                communicationModule,
                quizParticipationModule,
                settingsModule,
                lectureModule,
                pushModule,
                dbModule
            )
        }

        ArtemisNotificationChannel.entries.forEach { notificationChannel ->
            val channel = NotificationChannel(
                notificationChannel.id,
                getString(notificationChannel.title),
                notificationChannel.importance
            )
            channel.description = getString(notificationChannel.description)

            NotificationManagerCompat
                .from(this)
                .createNotificationChannel(channel)
        }

        registerActivityLifecycleCallbacks(this)
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .build()

    override fun onActivityResumed(activity: Activity) {
        currentActivity.value = activity
    }

    override fun onActivityPaused(activity: Activity) {
        if (currentActivity.value == activity) {
            currentActivity.value = null
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