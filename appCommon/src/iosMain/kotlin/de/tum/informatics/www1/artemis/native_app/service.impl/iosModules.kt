package de.tum.informatics.www1.artemis.native_app.service.impl

import de.tum.informatics.www1.artemis.native_app.service.SettingsProvider
import org.koin.dsl.module

val iosModules = module {
    single<SettingsProvider> { IosSettingsProviderImpl() }
}