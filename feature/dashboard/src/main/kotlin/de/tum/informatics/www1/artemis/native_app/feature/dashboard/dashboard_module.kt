package de.tum.informatics.www1.artemis.native_app.feature.dashboard

import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val dashboardModule = module {
    viewModelOf(::CourseOverviewViewModel)
}