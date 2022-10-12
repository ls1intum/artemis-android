package de.tum.informatics.www1.artemis.native_app.android.ui.account.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.android.defaults.ArtemisInstances
import de.tum.informatics.www1.artemis.native_app.android.server_config.ProfileInfo
import de.tum.informatics.www1.artemis.native_app.android.service.AccountService
import de.tum.informatics.www1.artemis.native_app.android.service.ServerCommunicationProvider
import de.tum.informatics.www1.artemis.native_app.android.util.DataState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * View model to handle the login process.
 */
class LoginViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val accountService: AccountService,
    private val serverCommunicationProvider: ServerCommunicationProvider
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

    val userAcceptedTerms: Flow<Boolean> =
        savedStateHandle.getStateFlow(USER_ACCEPTED_TERMS_KEY, false)

    val loginButtonEnabled: Flow<Boolean> =
        combine(
            username,
            password,
            userAcceptedTerms,
            serverCommunicationProvider.serverProfileInfo
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

    fun login(onSuccess: () -> Unit, onFailure: () -> Unit) {
        viewModelScope.launch {
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