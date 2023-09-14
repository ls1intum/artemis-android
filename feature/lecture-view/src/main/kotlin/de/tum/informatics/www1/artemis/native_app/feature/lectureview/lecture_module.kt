package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import de.tum.informatics.www1.artemis.native_app.feature.lectureview.service.LectureService
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.service.impl.LectureServiceImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val lectureModule = module {
    viewModel {
        LectureViewModel(
            it.get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    single<LectureService> { LectureServiceImpl(get()) }
}