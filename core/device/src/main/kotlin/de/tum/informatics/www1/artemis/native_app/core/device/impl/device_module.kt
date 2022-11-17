package de.tum.informatics.www1.artemis.native_app.core.device.impl

import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val deviceModule = module {
    single<NetworkStatusProvider> { NetworkStatusProviderImpl(androidContext()) }
}