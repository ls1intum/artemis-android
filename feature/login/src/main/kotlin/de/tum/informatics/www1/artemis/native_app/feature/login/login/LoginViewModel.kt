package de.tum.informatics.www1.artemis.native_app.feature.login.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.onSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.ServerProfileInfoService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.serverUrlStateFlow
import de.tum.informatics.www1.artemis.native_app.feature.login.BaseAccountViewModel
import de.tum.informatics.www1.artemis.native_app.feature.login.service.network.LoginService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * View model to handle the login process.
 */
class LoginViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val accountService: AccountService,
    private val loginService: LoginService,
    private val pushNotificationConfigurationService: PushNotificationConfigurationService,
    serverConfigurationService: ServerConfigurationService,
    serverProfileInfoService: ServerProfileInfoService,
    networkStatusProvider: NetworkStatusProvider,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : BaseAccountViewModel(serverConfigurationService, networkStatusProvider, serverProfileInfoService) {

    companion object {
        private const val USERNAME_KEY = "username"
        private const val PASSWORD_KEY = "password"
        private const val REMEMBER_ME_KEY = "rememberMe"
        private const val USER_ACCEPTED_TERMS_KEY = "rememberMe"
    }

    val username: StateFlow<String> = savedStateHandle.getStateFlow(USERNAME_KEY, "")

    val password: StateFlow<String> = savedStateHandle.getStateFlow(PASSWORD_KEY, "")

    val rememberMe: StateFlow<Boolean> = savedStateHandle.getStateFlow(REMEMBER_ME_KEY, true)

    val hasUserAcceptedTerms: StateFlow<Boolean> =
        savedStateHandle.getStateFlow(USER_ACCEPTED_TERMS_KEY, false)

    val loginButtonEnabled: StateFlow<Boolean> =
        combine(
            username,
            password,
            hasUserAcceptedTerms,
            serverProfileInfo
        ) { username, password, userAcceptedTerms, serverProfileInfo ->
            val needsToAcceptTerms = when (serverProfileInfo) {
                is DataState.Success -> serverProfileInfo.data.needsToAcceptTerms
                else -> false
            }
            username.isNotBlank() && password.isNotBlank() && (!needsToAcceptTerms || userAcceptedTerms)
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val serverUrl: StateFlow<String> = serverUrlStateFlow(serverConfigurationService)

    fun updateUsername(newUsername: String) {
        savedStateHandle[USERNAME_KEY] = newUsername
    }

    fun updatePassword(newPassword: String) {
        savedStateHandle[PASSWORD_KEY] = newPassword
    }

    fun updateRememberMe(newRememberMe: Boolean) {
        savedStateHandle[REMEMBER_ME_KEY] = newRememberMe
    }

    fun updateUserAcceptedTerms(newUserAcceptedTerms: Boolean) {
        savedStateHandle[USER_ACCEPTED_TERMS_KEY] = newUserAcceptedTerms
    }

    fun login(): Deferred<Boolean> {
        return viewModelScope.async(coroutineContext) {
            val serverUrl = serverUrl.value
            val rememberMe = rememberMe.first()

            val hasToRegisterForPushNotifications =
                pushNotificationConfigurationService.getArePushNotificationsEnabledFlow(serverUrl)
                    .first()

            loginService.loginWithCredentials(
                username.first(),
                password.first(),
                rememberMe,
                serverUrl
            )
                .then {
                    if (hasToRegisterForPushNotifications) {
                        val wasSuccess =
                            pushNotificationConfigurationService.updateArePushNotificationEnabled(
                                true,
                                serverUrl,
                                it.idToken
                            )

                        if (wasSuccess) NetworkResponse.Response(it) else NetworkResponse.Failure(
                            RuntimeException("Could not register for push notifications")
                        )
                    } else NetworkResponse.Response(it)
                }
                .onSuccess {
                    accountService.storeAccessToken(it.idToken, rememberMe)
                }
                .bind { true }
                .or(false)
        }
    }
}
