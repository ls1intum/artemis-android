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
    single<FaqRemoteService> { FaqRemoteServiceImpl(get()) }
    single<FaqRepository> { FaqRepositoryImpl(get(), get(), get(), get()) }

    viewModel { params ->
        FaqOverviewViewModel(
            params[0],
            get(),
        )
    }

    viewModel { params ->
        FaqDetailViewModel(
            params[0],
            params[1],
            get(),
            get()
        )
    }
}