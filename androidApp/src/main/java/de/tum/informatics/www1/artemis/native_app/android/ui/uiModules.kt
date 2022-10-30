package de.tum.informatics.www1.artemis.native_app.android.ui

import de.tum.informatics.www1.artemis.native_app.android.ui.courses.dashboard.CourseOverviewViewModel
import de.tum.informatics.www1.artemis.native_app.android.ui.account.login.LoginViewModel
import de.tum.informatics.www1.artemis.native_app.android.ui.account.register.RegisterViewModel
import de.tum.informatics.www1.artemis.native_app.android.ui.account.AccountViewModel
import de.tum.informatics.www1.artemis.native_app.android.ui.courses.course.CourseViewModel
import de.tum.informatics.www1.artemis.native_app.android.ui.courses.course_registation.RegisterForCourseViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::CourseOverviewViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::AccountViewModel)
    viewModelOf(::RegisterForCourseViewModel)
    viewModel { parametersHolder ->
        CourseViewModel(parametersHolder.get(), get(), get(), get(), get(), get())
    }
}