package de.tum.informatics.www1.artemis.native_app.android

import android.app.Application
import de.tum.informatics.www1.artemis.native_app.android.service.impl.commonModule
import de.tum.informatics.www1.artemis.native_app.android.service.impl.environmentModule
import de.tum.informatics.www1.artemis.native_app.android.ui.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ArtemisApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@ArtemisApplication)

            modules(
                commonModule,
                environmentModule,
                viewModelModule
            )
        }
    }
}