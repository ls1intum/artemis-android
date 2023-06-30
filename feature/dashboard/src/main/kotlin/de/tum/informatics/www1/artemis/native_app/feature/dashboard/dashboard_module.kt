package de.tum.informatics.www1.artemis.native_app.feature.dashboard

import de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.DashboardService
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.impl.DashboardServiceImpl
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val dashboardModule = module {
    viewModelOf(::CourseOverviewViewModel)
    single<DashboardService> { DashboardServiceImpl(get()) }
}