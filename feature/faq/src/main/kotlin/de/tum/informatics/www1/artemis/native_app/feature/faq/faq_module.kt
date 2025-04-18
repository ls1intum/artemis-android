package de.tum.informatics.www1.artemis.native_app.feature.faq

import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.FaqRepository
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.FaqRepositoryImpl
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.remote.FaqRemoteService
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.remote.FaqRemoteServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.faq.ui.detail.FaqDetailViewModel
import de.tum.informatics.www1.artemis.native_app.feature.faq.ui.overview.FaqOverviewViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val faqModule = module {
    single<FaqRemoteService> { FaqRemoteServiceImpl(get(), get()) }
    single<FaqRepository> { FaqRepositoryImpl(get(), get()) }

    viewModel { _ ->
        FaqOverviewViewModel(
            get(),
        )
    }

    viewModel { params ->
        FaqDetailViewModel(
            params[0],
            get(),
            get()
        )
    }
}