package de.tum.informatics.www1.artemis.native_app.android.db

import de.tum.informatics.www1.artemis.native_app.core.datastore.room.course.CourseDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.FaqDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.MetisDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.push.PushCommunicationDatabaseProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dbModule = module {
    single { DatabaseProvider(androidContext()) }
    single<MetisDatabaseProvider> { MetisDatabaseProviderImpl(get()) }
    single<PushCommunicationDatabaseProvider> { PushCommunicationDatabaseProviderImpl(get()) }
    single<CourseDatabaseProvider> { CourseDatabaseProviderImpl(get()) }
    single<FaqDatabaseProvider> { FaqDatabaseProviderImpl(get()) }
}