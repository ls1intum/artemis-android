package de.tum.informatics.www1.artemis.native_app.core.test

import de.tum.informatics.www1.artemis.native_app.core.common.commonModule
import de.tum.informatics.www1.artemis.native_app.core.data.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.artemis_context.ArtemisContextProviderImpl
import de.tum.informatics.www1.artemis.native_app.core.data.dataModule
import de.tum.informatics.www1.artemis.native_app.core.data.test.testDataModule
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.datastoreModule
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.TestServerConfigurationProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.test.uiTestModule
import de.tum.informatics.www1.artemis.native_app.core.websocket.websocketModule
import de.tum.informatics.www1.artemis.native_app.device.test.deviceTestModule
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import org.koin.dsl.module

@OptIn(DelicateCoroutinesApi::class)
val coreTestModules = listOf(
    commonModule,
    dataModule,
    testDataModule,
    datastoreModule,
    deviceTestModule,
    uiTestModule,
    websocketModule,
    module {
        single<ServerConfigurationService> { TestServerConfigurationProvider() }
        single<ArtemisContextProvider> { ArtemisContextProviderImpl(
            serverConfigurationService = get(),
            accountService = get(),
            accountDataService = get(),
            scope = GlobalScope
        ) }
    }
)