package de.tum.informatics.www1.artemis.native_app.core.datastore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ServerConfigurationServiceStub(
    override val serverUrl: Flow<String> = flowOf("https://example.com")
) : ServerConfigurationService {
    override suspend fun updateServerUrl(serverUrl: String) = Unit
}
