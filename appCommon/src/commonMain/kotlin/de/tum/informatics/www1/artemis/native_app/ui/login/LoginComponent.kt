package de.tum.informatics.www1.artemis.native_app.ui.login

import com.arkivanov.decompose.ComponentContext
import de.tum.informatics.www1.artemis.native_app.service.AccountService
import de.tum.informatics.www1.artemis.native_app.util.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class LoginComponent(
    componentContext: ComponentContext,
    private val onLoggedIn: () -> Unit
) :
    ComponentContext by componentContext,
    KoinComponent {

    private val accountService: AccountService = get()

    private val lifecycleScope = lifecycleScope()

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

    fun login(onFailure: () -> Unit) {
        lifecycleScope.launch {
            val response: AccountService.LoginResponse =
                accountService.login(username.first(), password.first(), rememberMe.first())

            if (response.isSuccessful) {
                onLoggedIn()
            } else {
                onFailure()
            }
        }
    }
}