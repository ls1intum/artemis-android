package de.tum.informatics.www1.artemis.native_app.core.websocket.impl

import de.tum.informatics.www1.artemis.native_app.core.websocket.LiveParticipationService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val websocketModule = module {
    singleOf(::WebsocketProvider)
    single<LiveParticipationService> {
        LiveParticipationServiceImpl(
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}