package de.tum.informatics.www1.artemis.native_app.core.datastore.impl

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.LoginService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.toKotlinInstant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

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
            .transformLatest { key ->
                val jwt = key?.let { JWT.decode(it) }

                if (jwt != null && isKeyValid(jwt)) {
                    val username = JWT.decode(key).subject.orEmpty()

                    emit(
                        AccountService.AuthenticationData.LoggedIn(
                            authToken = key,
                            username = username
                        )
                    )

                    // Wait till the expiration and the emit that the user is no longer logged in.
                    val expirationInstant = jwt.expiresAtAsInstant?.toKotlinInstant()
                    if (expirationInstant != null) {
                        val withTolerance = expirationInstant - 1.hours
                        val waitDuration: Duration = withTolerance - Clock.System.now()
                        delay(waitDuration)

                        emit(AccountService.AuthenticationData.NotLoggedIn)
                    }
                } else {
                    emit(AccountService.AuthenticationData.NotLoggedIn)
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

    private fun isKeyValid(jwt: DecodedJWT): Boolean {
        val expiresAt = jwt.expiresAtAsInstant?.toKotlinInstant() ?: return true

        val remainingValidDuration = expiresAt - Clock.System.now()
        return remainingValidDuration > 5.days
    }
}