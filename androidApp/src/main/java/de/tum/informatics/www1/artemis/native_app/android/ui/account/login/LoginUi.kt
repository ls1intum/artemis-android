package de.tum.informatics.www1.artemis.native_app.android.ui.account.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.tum.informatics.www1.artemis.native_app.android.R
import de.tum.informatics.www1.artemis.native_app.android.service.ServerCommunicationProvider
import de.tum.informatics.www1.artemis.native_app.android.util.DataState
import org.koin.androidx.compose.get

@Composable
fun LoginScreen(modifier: Modifier, viewModel: LoginViewModel, onLoggedIn: () -> Unit) {
    Scaffold(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            LoginUi(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                viewModel = viewModel,
                onLoggedIn = onLoggedIn
            )
        }
    }
}

@Composable
fun LoginUi(modifier: Modifier, viewModel: LoginViewModel, onLoggedIn: () -> Unit) {
    val username by viewModel.username.collectAsState(initial = "")
    val password by viewModel.password.collectAsState(initial = "")
    val rememberMe by viewModel.rememberMe.collectAsState(initial = false)
    val userAcceptedTerms by viewModel.userAcceptedTerms.collectAsState(initial = false)
    val isLoginEnabled by viewModel.loginButtonEnabled.collectAsState(initial = false)

    var displayLoginFailedDialog by rememberSaveable { mutableStateOf(false) }

    val serverCommunicationProvider: ServerCommunicationProvider = get()

    @Suppress("MoveVariableDeclarationIntoWhen")
    val profileInfo =
        serverCommunicationProvider.serverProfileInfo.collectAsState(initial = DataState.Loading()).value

    val accountName = when (profileInfo) {
        is DataState.Success -> profileInfo.data.accountName ?: ""
        else -> "" //Should not happen
    }

    val needsToAcceptTerms = when (profileInfo) {
        is DataState.Success -> profileInfo.data.needsToAcceptTerms
        else -> false //Should not happen
    }

    LoginUi(
        modifier = modifier,
        username = username,
        password = password,
        rememberMe = rememberMe,
        userAcceptedTerms = userAcceptedTerms,
        updateUsername = viewModel::updateUsername,
        updatePassword = viewModel::updatePassword,
        updateRememberMe = viewModel::updateRememberMe,
        updateUserAcceptedTerms = viewModel::updateUserAcceptedTerms,
        onClickLogin = {
            viewModel.login(
                onSuccess = onLoggedIn,
                onFailure = {
                    displayLoginFailedDialog = true
                }
            )
        },
        isLoginEnabled = isLoginEnabled,
        accountName = accountName,
        needsToAcceptTerms = needsToAcceptTerms
    )

    if (displayLoginFailedDialog) {
        AlertDialog(
            onDismissRequest = { displayLoginFailedDialog = false },
            title = { Text(text = stringResource(id = R.string.login_dialog_login_failed_title)) },
            text = { Text(text = stringResource(id = R.string.login_dialog_login_failed_message)) },
            confirmButton = {
                TextButton(onClick = { displayLoginFailedDialog = false }) {
                    Text(text = stringResource(id = R.string.login_dialog_login_failed_confirm))
                }
            }
        )
    }
}

/**
 * Displays the username and password text field and the remember me checkbox.
 * The user can trigger the login by clicking on the login button.
 *
 * @param accountName the account name from [de.tum.informatics.www1.artemis.native_app.android.server_config.ProfileInfo.accountName]
 * @param onClickLogin called when the user clicks the login button.
 */
@Composable
fun LoginUi(
    modifier: Modifier,
    username: String,
    password: String,
    rememberMe: Boolean,
    userAcceptedTerms: Boolean,
    updateUsername: (String) -> Unit,
    updatePassword: (String) -> Unit,
    updateRememberMe: (Boolean) -> Unit,
    updateUserAcceptedTerms: (Boolean) -> Unit,
    onClickLogin: () -> Unit,
    isLoginEnabled: Boolean,
    accountName: String,
    needsToAcceptTerms: Boolean
) {
    Column(modifier = modifier.then(Modifier.verticalScroll(rememberScrollState()))) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            text = stringResource(id = R.string.login_please_sign_in_account, accountName),
            fontSize = 22.sp,
            textAlign = TextAlign.Center
        )

        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val elementModifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth(0.8f)

            TextField(
                modifier = elementModifier,
                value = username,
                onValueChange = updateUsername,
                label = { Text(text = stringResource(id = R.string.login_username_label)) }
            )

            TextField(
                modifier = elementModifier,
                value = password,
                onValueChange = updatePassword,
                label = { Text(text = stringResource(id = R.string.login_password_label)) },
                visualTransformation = remember { PasswordVisualTransformation() }
            )

            CheckboxWithText(
                modifier = elementModifier,
                isChecked = rememberMe,
                text = stringResource(id = R.string.login_remember_me_label),
                onCheckedChanged = updateRememberMe
            )

            if (needsToAcceptTerms) {
                CheckboxWithText(
                    modifier = elementModifier,
                    isChecked = userAcceptedTerms,
                    text = stringResource(id = R.string.login_remember_me_label),
                    onCheckedChanged = updateUserAcceptedTerms
                )
            }

            Button(
                modifier = elementModifier,
                onClick = onClickLogin,
                enabled = isLoginEnabled
            ) {
                Text(text = stringResource(id = R.string.login_perform_login_button_text))
            }
        }
    }
}

@Composable
private fun CheckboxWithText(
    modifier: Modifier,
    isChecked: Boolean,
    text: String,
    onCheckedChanged: (Boolean) -> Unit
) {
    Row(modifier = modifier) {
        Checkbox(
            modifier = Modifier,
            checked = isChecked,
            onCheckedChange = onCheckedChanged,
        )

        Text(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            text = text
        )
    }
}