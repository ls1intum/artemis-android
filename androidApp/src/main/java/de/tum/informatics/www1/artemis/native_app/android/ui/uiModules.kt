package de.tum.informatics.www1.artemis.native_app.android.ui

import de.tum.informatics.www1.artemis.native_app.android.ui.courses_overview.CourseOverviewViewModel
import de.tum.informatics.www1.artemis.native_app.android.ui.login.LoginViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { LoginViewModel(get()) }
    viewModel { CourseOverviewViewModel(get(), get(), get(), get()) }
}