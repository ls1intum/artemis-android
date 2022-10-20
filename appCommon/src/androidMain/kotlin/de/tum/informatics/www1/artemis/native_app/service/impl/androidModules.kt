package de.tum.informatics.www1.artemis.native_app.service.impl

import de.tum.informatics.www1.artemis.native_app.service.AccountService
import de.tum.informatics.www1.artemis.native_app.service.SettingsProvider
import org.koin.dsl.module

val androidModules = module {
    single<SettingsProvider> { AndroidSettingsProvider(get()) }
}