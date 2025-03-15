package de.tum.informatics.www1.artemis.native_app.core.common

import de.tum.informatics.www1.artemis.native_app.core.common.app_version.AppVersionProvider
import de.tum.informatics.www1.artemis.native_app.core.common.app_version.AppVersionProviderImpl
import org.koin.dsl.module

val commonModule = module {
    single<AppVersionProvider> { AppVersionProviderImpl() }
}
