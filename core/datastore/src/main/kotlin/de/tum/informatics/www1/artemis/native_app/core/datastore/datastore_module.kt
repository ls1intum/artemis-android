package de.tum.informatics.www1.artemis.native_app.core.datastore

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.datastore.impl.AccountServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.datastore.impl.ArtemisContextProviderImpl
import de.tum.informatics.www1.artemis.native_app.core.datastore.impl.ServerConfigurationServiceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val datastoreModule = module {
    single<AccountService> { AccountServiceImpl(androidContext()) }
    single<ServerConfigurationService> { ServerConfigurationServiceImpl(androidContext()) }
    single<ArtemisContextProvider> { ArtemisContextProviderImpl(get(), get()) }
}