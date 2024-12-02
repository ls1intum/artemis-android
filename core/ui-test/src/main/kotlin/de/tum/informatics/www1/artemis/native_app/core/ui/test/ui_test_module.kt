package de.tum.informatics.www1.artemis.native_app.core.ui.test

import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.ArtemisImageProvider
import org.koin.dsl.module

val uiTestModule = module {
    single<ArtemisImageProvider> { ArtemisImageProviderStub() }
}