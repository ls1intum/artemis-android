package de.tum.informatics.www1.artemis.native_app.android.ui.account.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.android.R
import de.tum.informatics.www1.artemis.native_app.android.server_config.ProfileInfo
import de.tum.informatics.www1.artemis.native_app.android.service.ServerCommunicationProvider
import de.tum.informatics.www1.artemis.native_app.android.util.DataState
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

/**
 * Extension that returns the localized string if [RegisterViewModel.Status.errorText] is set.
 */
private val RegisterViewModel.Status.localizedErrorString: String?
    @Composable get() = this.errorText?.let { stringResource(id = it) }

@Composable
fun RegisterUi(modifier: Modifier, viewModel: RegisterViewModel = getViewModel()) {
    val serverCommunicationProvider: ServerCommunicationProvider = get()
    val profileInfo: DataState<ProfileInfo> =
        serverCommunicationProvider.serverProfileInfo.collectAsState(
            initial = DataState.Suspended()
        ).value

    //If we did not have a profile info, we could not be in this screen.
    if (profileInfo !is DataState.Success) return

    val firstName: String by viewModel.firstName.collectAsState(initial = "")
    val lastName: String by viewModel.lastName.collectAsState(initial = "")
    val username: String by viewModel.username.collectAsState(initial = "")
    val email: String by viewModel.email.collectAsState(initial = "")
    val password: String by viewModel.password.collectAsState(initial = "")
    val confirmPassword: String by viewModel.confirmPassword.collectAsState(initial = "")

    val firstNameStatus by viewModel.firstNameStatus.collectAsState(initial = RegisterViewModel.FirstNameStatus.OK)
    val lastNameStatus by viewModel.lastNameStatus.collectAsState(initial = RegisterViewModel.LastNameStatus.OK)
    val usernameStatus by viewModel.usernameStatus.collectAsState(initial = RegisterViewModel.UsernameStatus.OK)
    val emailStatus by viewModel.emailStatus.collectAsState(initial = RegisterViewModel.EmailStatus.OK)
    val passwordStatus by viewModel.passwordStatus.collectAsState(initial = RegisterViewModel.PasswordStatus.OK)
    val confirmPasswordStatus by viewModel.confirmPasswordStatus.collectAsState(initial = RegisterViewModel.ConfirmationPasswordStatus.OK)

    Column(modifier = modifier) {
        val textFieldModifier = Modifier
        val dividerModifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)

        ErrorTextField(
            modifier = textFieldModifier,
            value = firstName,
            onValueChange = viewModel::updateFirstName,
            label = stringResource(id = R.string.register_label_first_name),
            errorText = firstNameStatus.localizedErrorString
        )

        ErrorTextField(
            modifier = textFieldModifier,
            value = lastName,
            onValueChange = viewModel::updateLastName,
            label = stringResource(id = R.string.register_label_last_name),
            errorText = lastNameStatus.localizedErrorString
        )

        ErrorTextField(
            modifier = textFieldModifier,
            value = username,
            onValueChange = viewModel::updateUsername,
            label = stringResource(id = R.string.register_label_username),
            errorText = usernameStatus.localizedErrorString
        )

        Divider(modifier = dividerModifier)

        Text(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .fillMaxWidth(),
            text = stringResource(
                id = R.string.register_email_pattern,
                profileInfo.data.allowedEmailPatternReadable ?: ""
            )
        )

        ErrorTextField(
            modifier = textFieldModifier,
            value = email,
            onValueChange = viewModel::updateEmail,
            label = stringResource(id = R.string.register_label_email),
            errorText = emailStatus.localizedErrorString
        )

        Divider(modifier = dividerModifier)

        ErrorTextField(
            modifier = textFieldModifier,
            value = password,
            onValueChange = viewModel::updatePassword,
            label = stringResource(id = R.string.register_label_password),
            errorText = passwordStatus.localizedErrorString
        )

        ErrorTextField(
            modifier = textFieldModifier,
            value = confirmPassword,
            onValueChange = viewModel::updateConfirmPassword,
            label = stringResource(id = R.string.register_label_confirm_password),
            errorText = confirmPasswordStatus.localizedErrorString
        )
    }
}

@Composable
private fun ErrorTextField(
    modifier: Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    errorText: String?
) {
    Column(modifier = modifier) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            label = { Text(text = label) },
            isError = errorText != null
        )

        if (errorText != null) {
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}