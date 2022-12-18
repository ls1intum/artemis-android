package de.tum.informatics.www1.artemis.native_app.core.data.service

import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.BuildLogServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.CourseExerciseServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.LoginServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.ParticipationServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.ResultServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.ServerDataServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.courses.CourseRegistrationServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.courses.CourseServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.courses.DashboardServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.exercises.ExerciseServiceImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    singleOf(::JsonProvider)
    singleOf(::KtorProvider)

    single<CourseRegistrationService> { CourseRegistrationServiceImpl(get(), get()) }
    single<CourseService> { CourseServiceImpl(get(), get()) }
    single<DashboardService> { DashboardServiceImpl(get(), get()) }
    single<ExerciseService> { ExerciseServiceImpl(get(), get(), get()) }
    single<LoginService> { LoginServiceImpl(get()) }
    single<ServerDataService> { ServerDataServiceImpl(get(), get()) }
    single<ResultService> { ResultServiceImpl(get(), get()) }
    single<BuildLogService> { BuildLogServiceImpl(get(), get()) }
    single<CourseExerciseService> { CourseExerciseServiceImpl(get()) }
    single<ParticipationService> { ParticipationServiceImpl(get()) }
}