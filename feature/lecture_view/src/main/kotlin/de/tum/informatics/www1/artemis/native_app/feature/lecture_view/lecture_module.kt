package de.tum.informatics.www1.artemis.native_app.feature.lecture_view

import de.tum.informatics.www1.artemis.native_app.feature.lecture_view.service.LectureService
import de.tum.informatics.www1.artemis.native_app.feature.lecture_view.service.impl.LectureServiceImpl
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val lectureModule = module {
    viewModelOf(::LectureViewModel)
    single<LectureService> { LectureServiceImpl(get()) }
}