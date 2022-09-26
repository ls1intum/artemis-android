package de.tum.informatics.www1.artemis.native_app.android.service.impl

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.android.service.AccountService
import de.tum.informatics.www1.artemis.native_app.android.util.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.android.util.performNetworkCall
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class AccountServiceImpl(
    private val ktorProvider: KtorProvider,
    private val serverCommunicationProvider: ServerCommunicationProvider,
    private val context: Context
) : AccountService {

    companion object {
        private val JWT_KEY = stringPreferencesKey("jwt")
    }

    private val Context.accountSettingsStore by preferencesDataStore("account_settings")

    override val authenticationData: Flow<AccountService.AuthenticationData> =
        context
            .accountSettingsStore
            .data
            .map { data ->
                val key = data[JWT_KEY]
                //TODO: Verify if the key has expired, etc
                if (key != null) {
                    AccountService.AuthenticationData.LoggedIn(key)
                } else {
                    AccountService.AuthenticationData.NotLoggedIn
                }
            }

    override suspend fun login(username: String, password: String, rememberMe: Boolean): AccountService.LoginResponse {
        val tokenResponse = performNetworkCall {
            val body: LoginResponseBody =
                ktorProvider.ktorClient.post(serverCommunicationProvider.serverUrl.first()) {
                    url {
                        appendPathSegments("api", "authenticate")
                    }

                    contentType(ContentType.Application.Json)
                    setBody(LoginBody(username, password, rememberMe))
                }.body()

            body
        }

        return when (tokenResponse) {
            is NetworkResponse.Response -> {
                context.accountSettingsStore.edit { data ->
                    data[JWT_KEY] = tokenResponse.data.idToken
                }

                AccountService.LoginResponse(isSuccessful = true)
            }
            is NetworkResponse.Failure -> AccountService.LoginResponse(isSuccessful = false)
        }
    }

    @Serializable
    private data class LoginBody(
        val username: String,
        val password: String,
        val rememberMe: Boolean
    )

    @Serializable
    private data class LoginResponseBody(
        @SerialName("id_token") val idToken: String
    )

    override suspend fun logout() {
        context.accountSettingsStore.edit { data ->
            data.remove(JWT_KEY)
        }
    }
}