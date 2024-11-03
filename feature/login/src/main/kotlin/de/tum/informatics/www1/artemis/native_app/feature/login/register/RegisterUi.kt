package de.tum.informatics.www1.artemis.native_app.feature.login.register

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.server_config.ProfileInfo
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.common.ButtonWithLoadingAnimation
import de.tum.informatics.www1.artemis.native_app.feature.login.R
import de.tum.informatics.www1.artemis.native_app.feature.login.login.PasswordTextField
import kotlinx.coroutines.Deferred
import org.koin.androidx.compose.getViewModel

internal const val TEST_TAG_TEXT_FIELD_FIRST_NAME = "TEST_TAG_TEXT_FIELD_FIRST_NAME"
internal const val TEST_TAG_TEXT_FIELD_LAST_NAME = "TEST_TAG_TEXT_FIELD_LAST_NAME"
internal const val TEST_TAG_TEXT_FIELD_LOGIN_NAME = "TEST_TAG_TEXT_FIELD_LOGIN_NAME"
internal const val TEST_TAG_TEXT_FIELD_EMAIL = "TEST_TAG_TEXT_FIELD_EMAIL"
internal const val TEST_TAG_TEXT_FIELD_PASSWORD = "TEST_TAG_TEXT_FIELD_PASSWORD"
internal const val TEST_TAG_TEXT_FIELD_CONFIRM_PASSWORD = "TEST_TAG_TEXT_FIELD_CONFIRM_PASSWORD"

/**
 * Extension that returns the localized string if [RegisterViewModel.Status.errorText] is set.
 */
private val RegisterViewModel.Status.localizedErrorString: String?
    @Composable get() = this.errorText?.let { stringResource(id = it) }

@Composable
internal fun RegisterUi(
    modifier: Modifier,
    viewModel: RegisterViewModel = getViewModel(),
    onRegistered: () -> Unit
) {
    val profileInfo: DataState<ProfileInfo> = viewModel.serverProfileInfo.collectAsState().value

    //If we did not have a profile info, we could not be in this screen.
    if (profileInfo !is DataState.Success) return

    val firstName: String by viewModel.firstName.collectAsState()
    val lastName: String by viewModel.lastName.collectAsState()
    val username: String by viewModel.username.collectAsState()
    val email: String by viewModel.email.collectAsState()
    val password: String by viewModel.password.collectAsState()
    val confirmPassword: String by viewModel.confirmPassword.collectAsState()

    val firstNameStatus by viewModel.firstNameStatus.collectAsState(initial = RegisterViewModel.FirstNameStatus.OK)
    val lastNameStatus by viewModel.lastNameStatus.collectAsState(initial = RegisterViewModel.LastNameStatus.OK)
    val usernameStatus by viewModel.usernameStatus.collectAsState(initial = RegisterViewModel.UsernameStatus.OK)
    val emailStatus by viewModel.emailStatus.collectAsState(initial = RegisterViewModel.EmailStatus.OK)
    val passwordStatus by viewModel.passwordStatus.collectAsState(initial = RegisterViewModel.PasswordStatus.OK)
    val confirmPasswordStatus by viewModel.confirmPasswordStatus.collectAsState(initial = RegisterViewModel.ConfirmationPasswordStatus.OK)

    val isRegistrationAvailable by viewModel.isRegistrationAvailable.collectAsState()

    var registerDeferred: Deferred<RegisterViewModel.RegistrationResponse>? by remember {
        mutableStateOf(
            null
        )
    }

    var displayPasswordMismatchDialog: Boolean by remember { mutableStateOf(false) }
    var displayRegisterFailedDialog: Boolean by remember { mutableStateOf(false) }

    AwaitDeferredCompletion(job = registerDeferred) { response ->
        when (response) {
            RegisterViewModel.RegistrationResponse.PASSWORD_MISMATCH -> displayPasswordMismatchDialog = true
            RegisterViewModel.RegistrationResponse.SUCCESS -> onRegistered()
            RegisterViewModel.RegistrationResponse.FAILURE -> displayRegisterFailedDialog = true
        }

        registerDeferred = null
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val textFieldModifier = Modifier
        val dividerModifier = Modifier

        ErrorTextField(
            modifier = textFieldModifier.testTag(TEST_TAG_TEXT_FIELD_FIRST_NAME),
            value = firstName,
            onValueChange = viewModel::updateFirstName,
            label = stringResource(id = R.string.register_label_first_name),
            errorText = firstNameStatus.localizedErrorString,
        )

        ErrorTextField(
            modifier = textFieldModifier.testTag(TEST_TAG_TEXT_FIELD_LAST_NAME),
            value = lastName,
            onValueChange = viewModel::updateLastName,
            label = stringResource(id = R.string.register_label_last_name),
            errorText = lastNameStatus.localizedErrorString,
        )

        ErrorTextField(
            modifier = textFieldModifier.testTag(TEST_TAG_TEXT_FIELD_LOGIN_NAME),
            value = username,
            onValueChange = viewModel::updateUsername,
            label = stringResource(id = R.string.register_label_username),
            errorText = usernameStatus.localizedErrorString,
        )

        Divider(modifier = dividerModifier)

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(
                id = R.string.register_email_pattern,
                profileInfo.data.allowedEmailPatternReadable ?: ""
            )
        )

        ErrorTextField(
            modifier = textFieldModifier.testTag(TEST_TAG_TEXT_FIELD_EMAIL),
            value = email,
            onValueChange = viewModel::updateEmail,
            label = stringResource(id = R.string.register_label_email),
            errorText = emailStatus.localizedErrorString,
            keyboardType = KeyboardType.Email
        )

        Divider(modifier = dividerModifier)

        ErrorTextField(
            modifier = textFieldModifier.testTag(TEST_TAG_TEXT_FIELD_PASSWORD),
            value = password,
            onValueChange = viewModel::updatePassword,
            label = stringResource(id = R.string.register_label_password),
            errorText = passwordStatus.localizedErrorString,
            keyboardType = KeyboardType.Password
        )

        ErrorTextField(
            modifier = textFieldModifier.testTag(TEST_TAG_TEXT_FIELD_CONFIRM_PASSWORD),
            value = confirmPassword,
            onValueChange = viewModel::updateConfirmPassword,
            label = stringResource(id = R.string.register_label_confirm_password),
            errorText = confirmPasswordStatus.localizedErrorString,
            keyboardType = KeyboardType.Password,
            isLastTextField = true
        )

        ButtonWithLoadingAnimation(
            modifier = Modifier,
            onClick = { registerDeferred = viewModel.register() },
            isLoading = registerDeferred != null,
            enabled = isRegistrationAvailable
        ) {
            Text(text = stringResource(id = R.string.register_button_register))
        }
    }

    if (displayPasswordMismatchDialog) {
        TextAlertDialog(
            title = null,
            text = stringResource(id = R.string.register_password_mismatch_dialog_message),
            confirmButtonText = stringResource(id = R.string.register_password_mismatch_dialog_positive),
            dismissButtonText = null,
            onPressPositiveButton = { displayPasswordMismatchDialog = false },
            onDismissRequest = {}
        )
    }

    if (displayRegisterFailedDialog) {
        TextAlertDialog(
            title = null,
            text = stringResource(id = R.string.register_failed_dialog_message),
            confirmButtonText = stringResource(id = R.string.register_failed_dialog_positive),
            dismissButtonText = null,
            onPressPositiveButton = { displayRegisterFailedDialog = false },
            onDismissRequest = {}
        )
    }
}

@Composable
private fun ErrorTextField(
    modifier: Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    errorText: String?,
    keyboardType: KeyboardType = KeyboardOptions.Default.keyboardType,
    isLastTextField: Boolean = false
) {
    val imeAction = if (isLastTextField) ImeAction.Done else ImeAction.Next

    Column(modifier = modifier) {
        if (keyboardType == KeyboardType.Password) {
            PasswordTextField(
                modifier = Modifier.fillMaxWidth(),
                password = value,
                label = label,
                updatePassword = onValueChange,
                imeAction = imeAction,
            )
        } else {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = onValueChange,
                label = { Text(text = label) },
                isError = errorText != null,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = keyboardType,
                    imeAction = imeAction
                ),
            )
        }

        AnimatedVisibility(visible = errorText != null) {
            Text(
                text = errorText.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}