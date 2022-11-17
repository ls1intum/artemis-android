package de.tum.informatics.www1.artemis.native_app.core.datastore.impl

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.android.model.account.Account
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.data.service.LoginService
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import kotlinx.coroutines.flow.*

internal class AccountServiceImpl(
    private val context: Context,
    private val loginService: LoginService,
    private val serverConfigurationService: ServerConfigurationService,
    private val serverDataService: ServerDataService
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
            inMemoryJWT,
            serverConfigurationService.serverUrl
        ) { storedJWT: String?, inMemoryJWT, serverUrl ->
            //The inMemoryJWT is always preferred
            (inMemoryJWT ?: storedJWT) to serverUrl
        }.transformLatest { (key: String?, serverUrl: String) ->
            //TODO: Verify if the key has expired, etc
            if (key != null) {
                emitAll(
                    serverDataService
                        .getAccountData(serverUrl, key)
                        .map { AccountService.AuthenticationData.LoggedIn(key, it) }
                )
            } else {
                emit(AccountService.AuthenticationData.NotLoggedIn)
            }
        }

    override suspend fun login(
        username: String,
        password: String,
        rememberMe: Boolean
    ): AccountService.LoginResponse {
        return when (val tokenResponse = loginService.login(
            username,
            password,
            rememberMe,
            serverConfigurationService.serverUrl.first()
        )) {
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

    override suspend fun logout() {
        inMemoryJWT.value = null

        context.accountSettingsStore.edit { data ->
            data.remove(JWT_KEY)
        }
    }
}