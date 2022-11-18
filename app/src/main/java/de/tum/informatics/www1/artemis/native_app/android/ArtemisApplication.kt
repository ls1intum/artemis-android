package de.tum.informatics.www1.artemis.native_app.android

import android.app.Application
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.dataModule
import de.tum.informatics.www1.artemis.native_app.core.datastore.impl.datastoreModule
import de.tum.informatics.www1.artemis.native_app.core.device.impl.deviceModule
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.websocketModule
import de.tum.informatics.www1.artemis.native_app.feature.course_registration.courseRegistrationModule
import de.tum.informatics.www1.artemis.native_app.feature.course_view.courseViewModule
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.dashboardModule
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.exerciseViewModule
import de.tum.informatics.www1.artemis.native_app.feature.login.loginModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ArtemisApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@ArtemisApplication)

            modules(
                dataModule,
                datastoreModule,
                deviceModule,
                websocketModule,
                courseRegistrationModule,
                courseViewModule,
                dashboardModule,
                loginModule,
                exerciseViewModule
            )
        }
    }
}