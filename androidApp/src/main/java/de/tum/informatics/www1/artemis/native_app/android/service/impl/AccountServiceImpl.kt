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
import kotlinx.coroutines.flow.*
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

    /**
     * Only set if the user logged in without remember me.
     */
    private val inMemoryJWT = MutableStateFlow<String?>(null)

    override val authenticationData: Flow<AccountService.AuthenticationData> =
        combine(
            context.accountSettingsStore.data.map { it[JWT_KEY] },
            inMemoryJWT
        ) { storedJWT: String?, inMemoryJWT ->
            //The inMemoryJWT is always preferred
            inMemoryJWT ?: storedJWT
        }.map { key: String? ->
            //TODO: Verify if the key has expired, etc
            if (key != null) {
                AccountService.AuthenticationData.LoggedIn(key)
            } else {
                AccountService.AuthenticationData.NotLoggedIn
            }
        }

    override suspend fun login(
        username: String,
        password: String,
        rememberMe: Boolean
    ): AccountService.LoginResponse {
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
                //either store the token permanently, or just cache it in memory.
                if (rememberMe) {
                    context.accountSettingsStore.edit { data ->
                        data[JWT_KEY] = tokenResponse.data.idToken
                    }
                } else {
                    inMemoryJWT.value = tokenResponse.data.idToken
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
        inMemoryJWT.value = null

        context.accountSettingsStore.edit { data ->
            data.remove(JWT_KEY)
        }
    }
}