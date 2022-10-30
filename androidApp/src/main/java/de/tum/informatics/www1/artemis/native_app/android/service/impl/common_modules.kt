package de.tum.informatics.www1.artemis.native_app.android.service.impl

import de.tum.informatics.www1.artemis.native_app.android.service.*
import de.tum.informatics.www1.artemis.native_app.android.service.exercises.ExerciseService
import de.tum.informatics.www1.artemis.native_app.android.service.exercises.ParticipationService
import de.tum.informatics.www1.artemis.native_app.android.service.impl.courses.CourseRegistrationServiceImpl
import de.tum.informatics.www1.artemis.native_app.android.service.impl.courses.CourseServiceImpl
import de.tum.informatics.www1.artemis.native_app.android.service.impl.courses.DashboardServiceImpl
import de.tum.informatics.www1.artemis.native_app.android.service.impl.exercises.ExerciseServiceImpl
import de.tum.informatics.www1.artemis.native_app.android.service.impl.exercises.ParticipationServiceImpl
import de.tum.informatics.www1.artemis.native_app.android.service.student.CourseRegistrationService
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val userManagementModule = module {
    single<AccountService> { AccountServiceImpl(get(), get(), get(), androidContext()) }
    single<CourseRegistrationService> { CourseRegistrationServiceImpl(get()) }
}

val userContentModule = module {
    single<DashboardService> { DashboardServiceImpl(get()) }
    single<CourseService> { CourseServiceImpl(get()) }
    single<ParticipationService> { ParticipationServiceImpl(get()) }
    single<ExerciseService> { ExerciseServiceImpl(get()) }
}

val environmentModule = module {
    single<NetworkStatusProvider> { NetworkStatusProviderImpl(androidContext()) }
}

val communicationModule = module {
    singleOf(::JsonProvider)
    singleOf(::KtorProvider)
    singleOf(::WebsocketProvider)

    single<ServerCommunicationProvider> {
        ServerCommunicationProviderImpl(androidContext(), get(), get())
    }
}