package de.tum.informatics.www1.artemis.native_app.android.service.impl

import android.content.Context
import android.provider.ContactsContract.Profile
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.android.content.ProfileInfo
import de.tum.informatics.www1.artemis.native_app.android.defaults.ArtemisInstances
import de.tum.informatics.www1.artemis.native_app.android.service.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.android.service.ServerCommunicationProvider
import de.tum.informatics.www1.artemis.native_app.android.util.DataState
import de.tum.informatics.www1.artemis.native_app.android.util.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.android.util.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.android.util.retryOnInternet
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.*

/**
 * Provides data about which instance of artemis is communicated with.
 */
class ServerCommunicationProviderImpl(
    context: Context,
    private val ktorProvider: KtorProvider,
    private val networkStatusProvider: NetworkStatusProvider
) :
    ServerCommunicationProvider {

    private companion object {
        private val Context.serverCommunicationPreferences by preferencesDataStore("server_communication")

        private val SERVER_URL_KEY = stringPreferencesKey("server_url")
    }

    override val serverUrl: Flow<String> =
        context
            .serverCommunicationPreferences
            .data
            .map { data -> data[SERVER_URL_KEY] ?: ArtemisInstances.TUM_ARTEMIS.serverUrl }

    override val serverProfileInfo: Flow<DataState<ProfileInfo>> =
        serverUrl.transformLatest { serverUrl ->
            emitAll(
                retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                    fetchProfileInfo(serverUrl)
                }
            )
        }

    private suspend fun fetchProfileInfo(serverUrl: String): ProfileInfo {
        return ktorProvider.ktorClient.get(serverUrl) {
            url { appendPathSegments("management", "info") }

            accept(ContentType.Application.Json)
        }.body()
    }
}