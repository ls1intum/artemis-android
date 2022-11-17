package de.tum.informatics.www1.artemis.native_app.feature.login.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * View model to handle the login process.
 */
class LoginViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val accountService: AccountService,
    private val serverConfigurationService: ServerConfigurationService,
    private val serverDataService: ServerDataService,
) : ViewModel() {

    companion object {
        private const val USERNAME_KEY = "username"
        private const val PASSWORD_KEY = "password"
        private const val REMEMBER_ME_KEY = "rememberMe"
        private const val USER_ACCEPTED_TERMS_KEY = "rememberMe"
    }

    val username: Flow<String> = savedStateHandle.getStateFlow(USERNAME_KEY, "")

    val password: Flow<String> = savedStateHandle.getStateFlow(PASSWORD_KEY, "")

    val rememberMe: Flow<Boolean> = savedStateHandle.getStateFlow(REMEMBER_ME_KEY, false)

    val hasUserAcceptedTerms: Flow<Boolean> =
        savedStateHandle.getStateFlow(USER_ACCEPTED_TERMS_KEY, false)

    val loginButtonEnabled: Flow<Boolean> =
        combine(
            username,
            password,
            hasUserAcceptedTerms,
            serverConfigurationService.serverUrl.transformLatest { serverUrl ->
                emitAll(
                    serverDataService.getServerProfileInfo(serverUrl)
                )
            }
        ) { username, password, userAcceptedTerms, serverProfileInfo ->
            val needsToAcceptTerms = when (serverProfileInfo) {
                is DataState.Success -> serverProfileInfo.data.needsToAcceptTerms
                else -> false
            }
            username.isNotBlank() && password.isNotBlank() && (!needsToAcceptTerms || userAcceptedTerms)
        }

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

    fun login(onSuccess: () -> Unit, onFailure: () -> Unit): Job {
        return viewModelScope.launch {
            val response: AccountService.LoginResponse =
                accountService.login(username.first(), password.first(), rememberMe.first())

            if (response.isSuccessful) {
                onSuccess()
            } else {
                onFailure()
            }
        }
    }
}