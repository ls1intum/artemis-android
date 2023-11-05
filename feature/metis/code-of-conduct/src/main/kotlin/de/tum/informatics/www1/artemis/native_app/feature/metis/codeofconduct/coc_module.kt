package de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct

import de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.service.CodeOfConductService
import de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.service.CodeOfConductStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.service.impl.CodeOfConductServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.service.impl.CodeOfConductStorageServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.ui.CodeOfConductViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val cocModule = module {
    single<CodeOfConductService> {
        CodeOfConductServiceImpl(
            get()
        )
    }
    single<CodeOfConductStorageService> {
        CodeOfConductStorageServiceImpl(
            androidContext()
        )
    }

    viewModel { params ->
        CodeOfConductViewModel(
            params.get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}
