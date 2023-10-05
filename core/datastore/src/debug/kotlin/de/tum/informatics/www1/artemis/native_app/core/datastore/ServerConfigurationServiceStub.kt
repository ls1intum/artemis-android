package de.tum.informatics.www1.artemis.native_app.core.datastore

import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ServerConfigurationServiceStub(
    override val serverUrl: Flow<String> = flowOf("https://example.com"),
    override val hasUserSelectedInstance: Flow<Boolean> = flowOf(true)
) : ServerConfigurationService {
    override suspend fun updateServerUrl(serverUrl: String) = Unit
}
