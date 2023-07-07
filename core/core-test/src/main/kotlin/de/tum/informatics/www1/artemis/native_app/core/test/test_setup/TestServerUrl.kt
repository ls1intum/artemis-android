package de.tum.informatics.www1.artemis.native_app.core.test.test_setup

import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

val testServerUrl: String
    get() = System.getenv("serverUrl") ?: "http://localhost:9000"

suspend fun KoinComponent.setTestServerUrl() {
    val serverConfigurationService: ServerConfigurationService = get()
    runBlocking {
        serverConfigurationService.updateServerUrl(testServerUrl)
    }
}