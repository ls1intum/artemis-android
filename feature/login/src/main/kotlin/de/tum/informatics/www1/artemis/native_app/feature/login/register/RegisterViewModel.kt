package de.tum.informatics.www1.artemis.native_app.feature.login.register

import android.util.Patterns
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.defaults.Constants
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.account.R
import de.tum.informatics.www1.artemis.native_app.feature.login.BaseAccountViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Handle the registration ui.
 */
class RegisterViewModel(
    private val savedStateHandle: SavedStateHandle,
    serverConfigurationService: ServerConfigurationService,
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

    val firstName: Flow<String> = savedStateHandle.getStateFlow(FIRST_NAME_KEY, "")

    val firstNameStatus: Flow<FirstNameStatus> = firstName.map { firstName ->
        when {
            firstName.isEmpty() -> FirstNameStatus.NOT_PRESENT
            firstName.length < 2 -> FirstNameStatus.SHORT
            else -> FirstNameStatus.OK
        }
    }

    val lastName: Flow<String> = savedStateHandle.getStateFlow(LAST_NAME_KEY, "")

    val lastNameStatus: Flow<LastNameStatus> = lastName.map { lastName ->
        when {
            lastName.isEmpty() -> LastNameStatus.NOT_PRESENT
            lastName.length < 2 -> LastNameStatus.SHORT
            else -> LastNameStatus.OK
        }
    }

    val username: Flow<String> = savedStateHandle.getStateFlow(USERNAME_KEY, "")

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

    val email: Flow<String> = savedStateHandle.getStateFlow(EMAIL_KEY, "")

    val emailStatus: Flow<EmailStatus> = email.map { email ->
        when {
            email.isEmpty() -> EmailStatus.NOT_PRESENT
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> EmailStatus.INVALID
            else -> EmailStatus.OK
        }
    }

    val password: Flow<String> = savedStateHandle.getStateFlow(PASSWORD_KEY, "")

    val passwordStatus: Flow<PasswordStatus> = password.map { password ->
        when {
            password.isEmpty() -> PasswordStatus.NOT_PRESENT
            password.length < Constants.PASSWORD_MIN_LENGTH -> PasswordStatus.SHORT
            password.length > Constants.PASSWORD_MAX_LENGTH -> PasswordStatus.LONG
            else -> PasswordStatus.OK
        }
    }

    val confirmPassword: Flow<String> = savedStateHandle.getStateFlow(CONFIRM_PASSWORD_KEY, "")

    val confirmPasswordStatus: Flow<ConfirmationPasswordStatus> =
        confirmPassword.map { confirmPassword ->
            when {
                confirmPassword.isEmpty() -> ConfirmationPasswordStatus.NOT_PRESENT
                confirmPassword.length < Constants.PASSWORD_MIN_LENGTH -> ConfirmationPasswordStatus.SHORT
                confirmPassword.length > Constants.PASSWORD_MAX_LENGTH -> ConfirmationPasswordStatus.LONG
                else -> ConfirmationPasswordStatus.OK
            }
        }

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

    interface Status {
        /**
         * String resource id of the string that should be displayed on an erroneous status.
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