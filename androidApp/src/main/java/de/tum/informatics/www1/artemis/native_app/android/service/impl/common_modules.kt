package de.tum.informatics.www1.artemis.native_app.android.service.impl

import de.tum.informatics.www1.artemis.native_app.android.service.AccountService
import de.tum.informatics.www1.artemis.native_app.android.service.DashboardService
import de.tum.informatics.www1.artemis.native_app.android.service.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.android.service.ServerCommunicationProvider
import de.tum.informatics.www1.artemis.native_app.android.service.impl.courses.DashboardServiceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Module that can be used by both android app and the iOS app because it does not have any native dependencies.
 */
val commonModule = module {
    single<DashboardService> { DashboardServiceImpl(get()) }

    single { KtorProvider() }
    single<ServerCommunicationProvider> {
        ServerCommunicationProviderImpl(androidContext(), get(), get())
    }
    single<AccountService> { AccountServiceImpl(get(), get(), get()) }
}

val environmentModule = module {
    single<NetworkStatusProvider> { NetworkStatusProviderImpl(androidContext()) }
}