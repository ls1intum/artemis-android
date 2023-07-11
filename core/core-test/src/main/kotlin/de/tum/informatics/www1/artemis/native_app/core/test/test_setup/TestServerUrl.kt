package de.tum.informatics.www1.artemis.native_app.core.test.test_setup

import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

val testServerUrl: String
    get() = System.getenv("SERVER_URL") ?: "http://localhost:8080"

class TestServerConfigurationProvider : ServerConfigurationService {
    override val serverUrl: Flow<String> = flowOf(testServerUrl)

    override val hasUserSelectedInstance: Flow<Boolean> = flowOf(true)

    override suspend fun updateServerUrl(serverUrl: String) = Unit
}
