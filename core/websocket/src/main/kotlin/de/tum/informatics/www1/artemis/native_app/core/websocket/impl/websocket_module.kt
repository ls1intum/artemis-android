package de.tum.informatics.www1.artemis.native_app.core.websocket.impl

import de.tum.informatics.www1.artemis.native_app.core.websocket.ParticipationService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val websocketModule = module {
    singleOf(::WebsocketProvider)
    single<ParticipationService> {
        ParticipationServiceImpl(
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}