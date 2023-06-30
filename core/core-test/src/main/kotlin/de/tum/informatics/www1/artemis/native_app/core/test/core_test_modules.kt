package de.tum.informatics.www1.artemis.native_app.core.test

import de.tum.informatics.www1.artemis.native_app.core.data.test.testDataModule
import de.tum.informatics.www1.artemis.native_app.core.datastore.datastoreModule
import de.tum.informatics.www1.artemis.native_app.core.device.deviceModule
import de.tum.informatics.www1.artemis.native_app.core.ui.uiModule
import de.tum.informatics.www1.artemis.native_app.core.websocket.websocketModule
import de.tum.informatics.www1.artemis.native_app.device.test.deviceTestModule

val coreTestModules = listOf(
    testDataModule,
    datastoreModule,
    deviceTestModule,
    uiModule,
    websocketModule
)