package de.tum.informatics.www1.artemis.native_app.android

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.memory.MemoryCache
import de.tum.informatics.www1.artemis.native_app.core.common.ArtemisNotificationChannel
import de.tum.informatics.www1.artemis.native_app.core.common.CurrentActivityListener
import io.sentry.android.core.SentryAndroid
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class ArtemisApplication : Application(), SingletonImageLoader.Factory, CurrentActivityListener {

    override val currentActivity = MutableStateFlow<Activity?>(null)

    override fun onCreate() {
        super.onCreate()

        SentryAndroid.init(this) {
            it.dsn =
                if (BuildConfig.DEBUG) "" else "https://8b4d69ac628d4462995ee6178365541f@sentry.ase.in.tum.de/3"
        }

        startKoin {
            androidContext(this@ArtemisApplication)
            workManagerFactory()

            modules(appModule)
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

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
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