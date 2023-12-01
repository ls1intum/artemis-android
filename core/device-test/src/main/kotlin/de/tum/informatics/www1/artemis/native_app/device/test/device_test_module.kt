package de.tum.informatics.www1.artemis.native_app.device.test

import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import org.koin.dsl.module

val deviceTestModule = module {
    single<NetworkStatusProvider> { NetworkStatusProviderStub() }
}