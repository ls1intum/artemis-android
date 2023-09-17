package de.tum.informatics.www1.artemis.native_app.feature.metis.shared

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.impl.ConversationServiceImpl
import org.koin.dsl.module

val sharedConversationModule = module {
    single<ConversationService> {
        ConversationServiceImpl(
            get()
        )
    }
}