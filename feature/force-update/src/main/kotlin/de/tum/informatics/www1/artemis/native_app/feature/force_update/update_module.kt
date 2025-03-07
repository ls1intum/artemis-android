package de.tum.informatics.www1.artemis.native_app.feature.force_update

import de.tum.informatics.www1.artemis.native_app.feature.force_update.repository.UpdateRepository
import de.tum.informatics.www1.artemis.native_app.feature.force_update.service.UpdateService
import de.tum.informatics.www1.artemis.native_app.feature.force_update.service.UpdateServiceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val updateModule = module {
        single<UpdateService> { UpdateServiceImpl(get()) }

        single<UpdateRepository> {
            UpdateRepository(
                context = androidContext(),
                updateService = get(),
                get(),
                get()
            )
        }
    }
