package de.tum.informatics.www1.artemis.native_app.core.data.test

import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.test.service.TrustAllCertsKtorProvider
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val testDataModule = module {
    singleOf(::JsonProvider)
    single<KtorProvider> { TrustAllCertsKtorProvider(get()) }
}
