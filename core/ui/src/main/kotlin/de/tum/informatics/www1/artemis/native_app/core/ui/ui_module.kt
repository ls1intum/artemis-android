package de.tum.informatics.www1.artemis.native_app.core.ui

import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.ArtemisImageProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.ArtemisImageProviderImpl
import org.koin.dsl.module

val uiModule = module {
    single<ArtemisImageProvider> { ArtemisImageProviderImpl(get(), get()) }
}