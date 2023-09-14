package de.tum.informatics.www1.artemis.native_app.feature.courseregistration

import de.tum.informatics.www1.artemis.native_app.feature.courseregistration.service.CourseRegistrationService
import de.tum.informatics.www1.artemis.native_app.feature.courseregistration.service.impl.CourseRegistrationServiceImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val courseRegistrationModule = module {
    single<CourseRegistrationService> { CourseRegistrationServiceImpl(get()) }
    viewModel { RegisterForCourseViewModel(get(), get(), get(), get()) }
}