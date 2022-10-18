package de.tum.informatics.www1.artemis.native_app.android.service.impl

import de.tum.informatics.www1.artemis.native_app.android.service.*
import de.tum.informatics.www1.artemis.native_app.android.service.impl.courses.CourseRegistrationServiceImpl
import de.tum.informatics.www1.artemis.native_app.android.service.impl.courses.DashboardServiceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val userManagementModule = module {
    single<AccountService> { AccountServiceImpl(get(), get(), get(), androidContext()) }
    single<CourseRegistrationService> { CourseRegistrationServiceImpl(get()) }
}

val userContentModule = module {
    single<DashboardService> { DashboardServiceImpl(get()) }
}

val environmentModule = module {
    single<NetworkStatusProvider> { NetworkStatusProviderImpl(androidContext()) }
}

val communicationModule = module {
    single { KtorProvider() }

    single<ServerCommunicationProvider> {
        ServerCommunicationProviderImpl(androidContext(), get(), get())
    }
}