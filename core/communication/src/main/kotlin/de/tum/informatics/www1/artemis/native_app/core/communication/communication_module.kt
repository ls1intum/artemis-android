package de.tum.informatics.www1.artemis.native_app.core.communication

import de.tum.informatics.www1.artemis.native_app.core.communication.impl.MetisServiceImpl
import org.koin.dsl.module

val communicationModule = module {
    single<MetisService> { MetisServiceImpl(get(), get(), get()) }
}