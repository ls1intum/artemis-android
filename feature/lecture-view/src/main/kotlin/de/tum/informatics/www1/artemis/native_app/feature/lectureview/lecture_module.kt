package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import de.tum.informatics.www1.artemis.native_app.feature.lectureview.service.LectureService
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.service.impl.LectureServiceImpl
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val lectureModule = module {
    viewModel {
        LectureViewModel(
            lectureId = it.get(),
            networkStatusProvider = get(),
            lectureService = get(),
            serverConfigurationService = get(),
            accountService = get(),
            liveParticipationService = get(),
            savedStateHandle = get(),
            channelService = get(),
            serverTimeService = get(),
            courseExerciseService = get()
        )
    }
    single<LectureService> { LectureServiceImpl(get()) }
}