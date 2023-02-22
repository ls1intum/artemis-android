package de.tum.informatics.www1.artemis.native_app.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.memory.MemoryCache
import de.tum.informatics.www1.artemis.native_app.core.data.service.dataModule
import de.tum.informatics.www1.artemis.native_app.core.datastore.datastoreModule
import de.tum.informatics.www1.artemis.native_app.core.device.deviceModule
import de.tum.informatics.www1.artemis.native_app.core.ui.uiModule
import de.tum.informatics.www1.artemis.native_app.core.websocket.websocketModule
import de.tum.informatics.www1.artemis.native_app.feature.course_registration.courseRegistrationModule
import de.tum.informatics.www1.artemis.native_app.feature.course_view.courseViewModule
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.dashboardModule
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.exerciseViewModule
import de.tum.informatics.www1.artemis.native_app.feature.lecture_view.lectureModule
import de.tum.informatics.www1.artemis.native_app.feature.login.loginModule
import de.tum.informatics.www1.artemis.native_app.feature.metis.communicationModule
import de.tum.informatics.www1.artemis.native_app.feature.push.ArtemisNotificationChannel
import de.tum.informatics.www1.artemis.native_app.feature.push.pushModule
import de.tum.informatics.www1.artemis.native_app.feature.quiz.quizParticipationModule
import de.tum.informatics.www1.artemis.native_app.feature.settings.settingsModule
import io.sentry.Sentry
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import de.tum.informatics.www1.artemis.native_app.feature.push.R

class ArtemisApplication : Application(), ImageLoaderFactory {

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
                exerciseViewModule,
                communicationModule,
                quizParticipationModule,
                settingsModule,
                lectureModule,
                pushModule
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.push_notification_channel_name)
            val description = getString(R.string.push_notification_channel_descriptions)

            val channel = NotificationChannel(
                ArtemisNotificationChannel.id,
                name,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = description

            NotificationManagerCompat
                .from(this)
                .createNotificationChannel(channel)
        }
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .build()
}