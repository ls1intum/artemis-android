package de.tum.informatics.www1.artemis.native_app.core.datastore

import de.tum.informatics.www1.artemis.native_app.core.datastore.impl.AccountServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.datastore.impl.ServerConfigurationServiceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val datastoreModule = module {
    single<AccountService> { AccountServiceImpl(androidContext(), get(), get()) }
    single<ServerConfigurationService> { ServerConfigurationServiceImpl(androidContext()) }
}