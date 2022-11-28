package de.tum.informatics.www1.artemis.native_app.core.communication

import de.tum.informatics.www1.artemis.native_app.core.communication.impl.MetisServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.MetisViewModel
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val communicationModule = module {
    single<MetisService> { MetisServiceImpl(get(), get(), get()) }

    viewModel { params ->
        MetisViewModel(
            metisContext = params.get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}