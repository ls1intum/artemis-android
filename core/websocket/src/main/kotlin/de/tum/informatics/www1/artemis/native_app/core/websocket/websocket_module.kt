package de.tum.informatics.www1.artemis.native_app.core.websocket

import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.LiveParticipationServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.ServerTimeServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketProvider
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val websocketModule = module {
    single { WebsocketProvider(get(), get(), get(), get()) }
    single<LiveParticipationService> {
        LiveParticipationServiceImpl(
            get()
        )
    }

    single<ServerTimeService> { ServerTimeServiceImpl(get(), get(), get()) }
}