package de.tum.informatics.www1.artemis.native_app.core.data

import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProviderImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseExerciseService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.ExerciseService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.ParticipationService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.ServerTimeService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.impl.AccountDataServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.impl.CourseExerciseServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.impl.CourseServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.impl.ExerciseServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.impl.ParticipationServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.impl.ServerTimeServiceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    singleOf(::JsonProvider)
    single<KtorProvider> { KtorProviderImpl(get()) }

    single<CourseService> { CourseServiceImpl(get(), get()) }
    single<ExerciseService> { ExerciseServiceImpl(get(), get()) }
    single<AccountDataService> { AccountDataServiceImpl(androidContext(), get(), get(), get()) }
    single<CourseExerciseService> { CourseExerciseServiceImpl(get(), get()) }
    single<ParticipationService> { ParticipationServiceImpl(get()) }
    single<ServerTimeService> { ServerTimeServiceImpl(get()) }
}