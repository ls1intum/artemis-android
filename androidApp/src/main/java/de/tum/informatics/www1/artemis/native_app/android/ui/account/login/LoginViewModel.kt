package de.tum.informatics.www1.artemis.native_app.android.ui.account.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.android.defaults.ArtemisInstances
import de.tum.informatics.www1.artemis.native_app.android.service.AccountService
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
    private val accountService: AccountService
) : ViewModel() {

    companion object {
        private const val USERNAME_KEY = "username"
        private const val PASSWORD_KEY = "password"
        private const val REMEMBER_ME_KEY = "rememberMe"
    }

    val username: Flow<String> = savedStateHandle.getStateFlow(USERNAME_KEY, "")

    val password: Flow<String> = savedStateHandle.getStateFlow(PASSWORD_KEY, "")

    val rememberMe: Flow<Boolean> = savedStateHandle.getStateFlow(REMEMBER_ME_KEY, false)

    val loginButtonEnabled: Flow<Boolean> = combine(username, password) { username, password ->
        username.isNotBlank() && password.isNotBlank()
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