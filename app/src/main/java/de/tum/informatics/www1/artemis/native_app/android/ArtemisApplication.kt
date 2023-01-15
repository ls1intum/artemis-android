package de.tum.informatics.www1.artemis.native_app.android

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.memory.MemoryCache
import de.tum.informatics.www1.artemis.native_app.core.communication.communicationModule
import de.tum.informatics.www1.artemis.native_app.core.data.service.dataModule
import de.tum.informatics.www1.artemis.native_app.core.datastore.impl.datastoreModule
import de.tum.informatics.www1.artemis.native_app.core.device.deviceModule
import de.tum.informatics.www1.artemis.native_app.core.push_notification_settings.pushNotificationModule
import de.tum.informatics.www1.artemis.native_app.core.ui.uiModule
import de.tum.informatics.www1.artemis.native_app.core.websocket.websocketModule
import de.tum.informatics.www1.artemis.native_app.feature.course_registration.courseRegistrationModule
import de.tum.informatics.www1.artemis.native_app.feature.course_view.courseViewModule
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.dashboardModule
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.exerciseViewModule
import de.tum.informatics.www1.artemis.native_app.feature.lecture_view.lectureModule
import de.tum.informatics.www1.artemis.native_app.feature.login.loginModule
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.quizParticipationModule
import de.tum.informatics.www1.artemis.native_app.feature.settings.settingsModule
import io.sentry.Sentry
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ArtemisApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        Sentry.init {
            it.dsn = if (BuildConfig.DEBUG) "" else "https://8b4d69ac628d4462995ee6178365541f@sentry.ase.in.tum.de/3"
            it.isEnableUserInteractionBreadcrumbs = false
            it.isEnableUserInteractionTracing = false
        }

        startKoin {
            androidContext(this@ArtemisApplication)

            modules(
                dataModule,
                uiModule,
                datastoreModule,
                deviceModule,
                websocketModule,
                pushNotificationModule,
                courseRegistrationModule,
                courseViewModule,
                dashboardModule,
                loginModule,
                exerciseViewModule,
                communicationModule,
                quizParticipationModule,
                settingsModule,
                lectureModule
            )
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