package de.tum.informatics.www1.artemis.native_app.feature.course_registration

import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val courseRegistrationModule = module {
    viewModelOf(::RegisterForCourseViewModel)
}