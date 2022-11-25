package de.tum.informatics.www1.artemis.native_app.core.datastore.impl

import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val datastoreModule = module {
    single<AccountService> { AccountServiceImpl(androidContext(), get(), get(), get()) }
    single<ServerConfigurationService> { ServerConfigurationServiceImpl(androidContext()) }
    single { DatabaseProvider(androidContext()) }
}