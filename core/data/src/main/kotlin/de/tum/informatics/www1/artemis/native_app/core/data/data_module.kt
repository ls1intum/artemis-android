package de.tum.informatics.www1.artemis.native_app.core.data

import de.tum.informatics.www1.artemis.native_app.core.data.service.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.data.service.BuildLogService
import de.tum.informatics.www1.artemis.native_app.core.data.service.CourseExerciseService
import de.tum.informatics.www1.artemis.native_app.core.data.service.CourseRegistrationService
import de.tum.informatics.www1.artemis.native_app.core.data.service.CourseService
import de.tum.informatics.www1.artemis.native_app.core.data.service.ExerciseService
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.ParticipationService
import de.tum.informatics.www1.artemis.native_app.core.data.service.ResultService
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.AccountDataServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.BuildLogServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.CourseExerciseServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProviderImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.ParticipationServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.ResultServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.courses.CourseRegistrationServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.courses.CourseServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.exercises.ExerciseServiceImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    singleOf(::JsonProvider)
    single<KtorProvider> { KtorProviderImpl(get()) }

    single<CourseRegistrationService> { CourseRegistrationServiceImpl(get(), get()) }
    single<CourseService> { CourseServiceImpl(get()) }
    single<ExerciseService> { ExerciseServiceImpl(get(), get()) }
    single<AccountDataService> { AccountDataServiceImpl(get()) }
    single<ResultService> { ResultServiceImpl(get()) }
    single<BuildLogService> { BuildLogServiceImpl(get(), get()) }
    single<CourseExerciseService> { CourseExerciseServiceImpl(get()) }
    single<ParticipationService> { ParticipationServiceImpl(get()) }
}