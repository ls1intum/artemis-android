package de.tum.informatics.www1.artemis.native_app.core.test

import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketProvider
import kotlinx.coroutines.test.TestDispatcher
import org.koin.dsl.module

fun testWebsocketModule(testDispatcher: TestDispatcher) = module {
    single<WebsocketProvider> {
        WebsocketProvider(
            serverConfigurationService = get(),
            accountService = get(),
            jsonProvider = get(),
            networkStatusProvider = get(),
            coroutineContext = testDispatcher,
            maxReconnects = 0 // In tests we do not want to reconnect
        )
    }
}