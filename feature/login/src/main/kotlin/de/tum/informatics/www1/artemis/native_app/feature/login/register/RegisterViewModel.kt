package de.tum.informatics.www1.artemis.native_app.feature.login.register

import android.util.Patterns
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.defaults.Constants
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.account.R
import de.tum.informatics.www1.artemis.native_app.feature.login.BaseAccountViewModel
import de.tum.informatics.www1.artemis.native_app.feature.login.service.RegisterService
import io.ktor.http.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * Handle the registration ui.
 */
internal class RegisterViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val registerService: RegisterService,
    private val serverConfigurationService: ServerConfigurationService,
    serverDataService: ServerDataService,
    networkStatusProvider: NetworkStatusProvider
) : BaseAccountViewModel(serverConfigurationService, networkStatusProvider, serverDataService) {

    private companion object {
        private val usernameRegex = "^[a-zA-Z0-9]*".toRegex()

        private const val FIRST_NAME_KEY = "first_name"
        private const val LAST_NAME_KEY = "last_name"
        private const val USERNAME_KEY = "username"
        private const val EMAIL_KEY = "email"
        private const val PASSWORD_KEY = "password"
        private const val CONFIRM_PASSWORD_KEY = "confirm_password"
    }

    val firstName: StateFlow<String> = savedStateHandle.getStateFlow(FIRST_NAME_KEY, "")

    val firstNameStatus: Flow<FirstNameStatus> = firstName.map { firstName ->
        when {
            firstName.isEmpty() -> FirstNameStatus.NOT_PRESENT
            firstName.length < 2 -> FirstNameStatus.SHORT
            else -> FirstNameStatus.OK
        }
    }

    val lastName: StateFlow<String> = savedStateHandle.getStateFlow(LAST_NAME_KEY, "")

    val lastNameStatus: Flow<LastNameStatus> = lastName.map { lastName ->
        when {
            lastName.isEmpty() -> LastNameStatus.NOT_PRESENT
            lastName.length < 2 -> LastNameStatus.SHORT
            else -> LastNameStatus.OK
        }
    }

    val username: StateFlow<String> = savedStateHandle.getStateFlow(USERNAME_KEY, "")

    val usernameStatus: Flow<UsernameStatus> =
        username.map { username ->
            when {
                username.isEmpty() -> UsernameStatus.NOT_PRESENT
                username.length < Constants.USERNAME_MIN_LENGTH -> UsernameStatus.SHORT
                username.length > Constants.USERNAME_MAX_LENGTH -> UsernameStatus.LONG
                !username.matches(usernameRegex) -> UsernameStatus.INVALID
                else -> UsernameStatus.OK
            }
        }

    val email: StateFlow<String> = savedStateHandle.getStateFlow(EMAIL_KEY, "")

    val emailStatus: Flow<EmailStatus> =
        combine(email, serverProfileInfo.filterSuccess()) { email, serverProfileInfo ->
            val matchesAllowedEmailPattern =
                serverProfileInfo.allowedEmailPattern?.toPattern()?.matcher(email)?.matches() ?: true

            when {
                email.isEmpty() -> EmailStatus.NOT_PRESENT
                !Patterns.EMAIL_ADDRESS.matcher(email)
                    .matches() || !matchesAllowedEmailPattern -> EmailStatus.INVALID
                else -> EmailStatus.OK
            }
        }

    val password: StateFlow<String> = savedStateHandle.getStateFlow(PASSWORD_KEY, "")

    val passwordStatus: Flow<PasswordStatus> = password.map { password ->
        when {
            password.isEmpty() -> PasswordStatus.NOT_PRESENT
            password.length < Constants.PASSWORD_MIN_LENGTH -> PasswordStatus.SHORT
            password.length > Constants.PASSWORD_MAX_LENGTH -> PasswordStatus.LONG
            else -> PasswordStatus.OK
        }
    }

    val confirmPassword: StateFlow<String> = savedStateHandle.getStateFlow(CONFIRM_PASSWORD_KEY, "")

    val confirmPasswordStatus: Flow<ConfirmationPasswordStatus> =
        confirmPassword.map { confirmPassword ->
            when {
                confirmPassword.isEmpty() -> ConfirmationPasswordStatus.NOT_PRESENT
                confirmPassword.length < Constants.PASSWORD_MIN_LENGTH -> ConfirmationPasswordStatus.SHORT
                confirmPassword.length > Constants.PASSWORD_MAX_LENGTH -> ConfirmationPasswordStatus.LONG
                else -> ConfirmationPasswordStatus.OK
            }
        }

    val isRegistrationAvailable: StateFlow<Boolean> = combine(
        firstNameStatus,
        lastNameStatus,
        usernameStatus,
        emailStatus,
        passwordStatus,
        confirmPasswordStatus
    ) { statusArray ->
        statusArray
            .map { it.errorText == null }
            .fold(true, Boolean::and)
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun updateUsername(newUsername: String) {
        savedStateHandle[USERNAME_KEY] = newUsername
    }

    fun updateFirstName(newFirstName: String) {
        savedStateHandle[FIRST_NAME_KEY] = newFirstName
    }

    fun updateLastName(newLastName: String) {
        savedStateHandle[LAST_NAME_KEY] = newLastName
    }

    fun updateEmail(newEmail: String) {
        savedStateHandle[EMAIL_KEY] = newEmail
    }

    fun updatePassword(newPassword: String) {
        savedStateHandle[PASSWORD_KEY] = newPassword
    }

    fun updateConfirmPassword(newConfirmPassword: String) {
        savedStateHandle[CONFIRM_PASSWORD_KEY] = newConfirmPassword
    }

    fun register(
        onPasswordMismatch: () -> Unit,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ): Job {
        return viewModelScope.launch {
            if (password.value != confirmPassword.value) {
                onPasswordMismatch()
                return@launch
            }

            val response = registerService.register(
                account = User(
                    firstName = firstName.value,
                    lastName = lastName.value,
                    username = username.value,
                    email = email.value,
                    password = password.value,
                    langKey = Locale.getDefault().toLanguageTag()
                ),
                serverUrl = serverConfigurationService.serverUrl.first()
            )

            if (response is NetworkResponse.Response && response.data.isSuccess()) {
                onSuccess()
            } else {
                onFailure()
            }
        }
    }

    interface Status {
        /**
         * String resource id of the string that should be displayed on an erroneous status.
         * If no error text is provided, it is assumed that this is a success state
         */
        val errorText: Int?
    }

    enum class FirstNameStatus(@StringRes override val errorText: Int?) : Status {
        OK(null),
        NOT_PRESENT(R.string.register_error_text_first_name_not_present),
        SHORT(R.string.register_error_text_first_name_short)
    }

    enum class LastNameStatus(@StringRes override val errorText: Int?) : Status {
        OK(null),
        NOT_PRESENT(R.string.register_error_text_last_name_not_present),
        SHORT(R.string.register_error_text_last_name_short)
    }

    enum class UsernameStatus(@StringRes override val errorText: Int?) : Status {
        OK(null),
        NOT_PRESENT(R.string.register_error_text_username_not_present),
        SHORT(R.string.register_error_text_username_short),
        LONG(R.string.register_error_text_username_long),
        INVALID(R.string.register_error_text_username_invalid_pattern)
    }

    enum class EmailStatus(@StringRes override val errorText: Int?) : Status {
        OK(null),
        NOT_PRESENT(R.string.register_error_text_email_not_present),
        INVALID(R.string.register_error_text_email_invalid_pattern)
    }

    enum class PasswordStatus(@StringRes override val errorText: Int?) : Status {
        OK(null),
        NOT_PRESENT(R.string.register_error_text_password_not_present),
        SHORT(R.string.register_error_text_password_short),
        LONG(R.string.register_error_text_password_long),
    }

    enum class ConfirmationPasswordStatus(@StringRes override val errorText: Int?) : Status {
        OK(null),
        NOT_PRESENT(R.string.register_error_text_confirmation_password_not_present),
        SHORT(R.string.register_error_text_confirmation_password_short),
        LONG(R.string.register_error_text_confirmation_password_long),
    }
}