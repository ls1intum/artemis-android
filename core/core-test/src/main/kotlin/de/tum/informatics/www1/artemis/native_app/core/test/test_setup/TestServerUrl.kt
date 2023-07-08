package de.tum.informatics.www1.artemis.native_app.core.test.test_setup

import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

val testServerUrl: String
    get() = System.getenv("serverUrl") ?: "http://localhost:9000"

class TestServerConfigurationProvider : ServerConfigurationService {
    override val serverUrl: Flow<String> = flowOf(testServerUrl)

    override val hasUserSelectedInstance: Flow<Boolean> = flowOf(true)

    override suspend fun updateServerUrl(serverUrl: String) = Unit
}
