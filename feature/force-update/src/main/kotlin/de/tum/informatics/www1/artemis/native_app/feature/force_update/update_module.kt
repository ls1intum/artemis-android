package de.tum.informatics.www1.artemis.native_app.feature.force_update

import de.tum.informatics.www1.artemis.native_app.feature.force_update.repository.UpdateRepository
import de.tum.informatics.www1.artemis.native_app.feature.force_update.service.UpdateService
import de.tum.informatics.www1.artemis.native_app.feature.force_update.service.UpdateServiceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val updateModule = module {
        single<UpdateService> { UpdateServiceImpl(get()) }

        single { (versionCode: Int) ->
            UpdateRepository(
                context = androidContext(),
                updateService = get(),
                versionCode = versionCode,
                get(),
                get()
            )
        }

        viewModel { (versionCode: Int, openPlayStore: () -> Unit) ->
            UpdateViewModel(
                updateRepository = get { parametersOf(versionCode) },
                openPlayStore = openPlayStore
            )
        }
    }
