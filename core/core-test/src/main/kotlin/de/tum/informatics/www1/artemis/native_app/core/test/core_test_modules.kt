package de.tum.informatics.www1.artemis.native_app.core.test

import de.tum.informatics.www1.artemis.native_app.core.data.dataModule
import de.tum.informatics.www1.artemis.native_app.core.data.test.testDataModule
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.datastoreModule
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.TestServerConfigurationProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.test.uiTestModule
import de.tum.informatics.www1.artemis.native_app.core.websocket.websocketModule
import de.tum.informatics.www1.artemis.native_app.device.test.deviceTestModule
import org.koin.dsl.module

val coreTestModules = listOf(
    dataModule,
    testDataModule,
    datastoreModule,
    deviceTestModule,
    uiTestModule,
    websocketModule,
    module {
        single<ServerConfigurationService> { TestServerConfigurationProvider() }
    }
)