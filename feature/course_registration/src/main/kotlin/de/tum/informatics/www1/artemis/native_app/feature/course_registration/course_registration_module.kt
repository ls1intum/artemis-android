package de.tum.informatics.www1.artemis.native_app.feature.course_registration

import de.tum.informatics.www1.artemis.native_app.feature.course_registration.service.CourseRegistrationService
import de.tum.informatics.www1.artemis.native_app.feature.course_registration.service.impl.CourseRegistrationServiceImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val courseRegistrationModule = module {
    single<CourseRegistrationService> { CourseRegistrationServiceImpl(get()) }
    viewModel { RegisterForCourseViewModel(get(), get(), get(), get()) }
}