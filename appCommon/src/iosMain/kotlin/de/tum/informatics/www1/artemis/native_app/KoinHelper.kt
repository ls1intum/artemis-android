package de.tum.informatics.www1.artemis.native_app

import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import de.tum.informatics.www1.artemis.native_app.service.impl.commonModule
import de.tum.informatics.www1.artemis.native_app.service.impl.iosModules
import org.koin.core.context.startKoin

fun initKoin() {
    LifecycleRegistry()

    startKoin {
        modules(commonModule, iosModules)
    }
}