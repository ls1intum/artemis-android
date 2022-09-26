package de.tum.informatics.www1.artemis.native_app.android.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.android.service.AccountService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * View model to handle the login process.
 */
class LoginViewModel(private val accountService: AccountService) : ViewModel() {

    private val _username = MutableStateFlow("")
    val username: Flow<String> = _username

    private val _password = MutableStateFlow("")
    val password: Flow<String> = _password

    private val _rememberMe = MutableStateFlow(false)
    val rememberMe: Flow<Boolean> = _rememberMe

    val loginButtonEnabled: Flow<Boolean> = combine(username, password) { username, password ->
        username.isNotBlank() && password.isNotBlank()
    }

    fun updateUsername(newUsername: String) {
        _username.value = newUsername
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }

    fun updateRememberMe(newRememberMe: Boolean) {
        _rememberMe.value = newRememberMe
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