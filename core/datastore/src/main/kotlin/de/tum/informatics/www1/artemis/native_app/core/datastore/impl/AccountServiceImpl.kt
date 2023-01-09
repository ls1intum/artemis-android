package de.tum.informatics.www1.artemis.native_app.core.datastore.impl

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.auth0.jwt.JWT
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.LoginService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.datetime.Clock
import kotlinx.datetime.toKotlinInstant
import kotlin.time.Duration.Companion.days

@OptIn(DelicateCoroutinesApi::class)
internal class AccountServiceImpl(
    private val context: Context,
    private val loginService: LoginService,
    private val serverConfigurationService: ServerConfigurationService,
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
            (inMemoryJWT ?: storedJWT)
        }
            .mapLatest { key ->
                if (key != null && isKeyValid(key)) {
                    val username = JWT.decode(key).subject.orEmpty()

                    AccountService.AuthenticationData.LoggedIn(authToken = key, username = username)
                } else {
                    AccountService.AuthenticationData.NotLoggedIn
                }
            }
            .shareIn(
                GlobalScope,
                started = SharingStarted.Eagerly,
                replay = 1
            )

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

    private fun isKeyValid(key: String): Boolean {
        val decodedKey = JWT.decode(key)
        val expiresAt = decodedKey.expiresAtAsInstant?.toKotlinInstant() ?: return true

        val remainingValidDuration = expiresAt - Clock.System.now()
        return remainingValidDuration > 5.days
    }
}