package de.tum.informatics.www1.artemis.native_app.android.db

import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.push.PushCommunicationDatabaseProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dbModule = module {
    single { DatabaseProvider(androidContext()) }
    single<MetisDatabaseProvider> { MetisDatabaseProviderImpl(get()) }
    single<PushCommunicationDatabaseProvider> { PushCommunicationDatabaseProviderImpl(get()) }
}