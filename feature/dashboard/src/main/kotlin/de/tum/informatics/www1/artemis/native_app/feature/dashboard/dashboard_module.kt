package de.tum.informatics.www1.artemis.native_app.feature.dashboard

import de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.BetaHintService
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.DashboardService
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.impl.BetaHintServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.impl.DashboardServiceImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val dashboardModule = module {
    viewModel { CourseOverviewViewModel(get(), get(), get(), get()) }
    single<DashboardService> { DashboardServiceImpl(get()) }
    single<BetaHintService> { BetaHintServiceImpl(get()) }
}