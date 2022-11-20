package de.tum.informatics.www1.artemis.native_app.feature.login.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import de.tum.informatics.www1.artemis.native_app.core.model.server_config.ProfileInfo
import de.tum.informatics.www1.artemis.native_app.core.model.server_config.Saml2Config
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.feature.account.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.transformLatest
import org.koin.androidx.compose.get

@Composable
internal fun LoginScreen(modifier: Modifier, viewModel: LoginViewModel, onLoggedIn: () -> Unit) {
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

private fun <T> fromProfileInfo(
    dataState: DataState<ProfileInfo>,
    default: T,
    onSuccess: (ProfileInfo) -> T
): T {
    return when (dataState) {
        is DataState.Success -> onSuccess(dataState.data)
        else -> default
    }
}

@Composable
fun LoginUi(modifier: Modifier, viewModel: LoginViewModel, onLoggedIn: () -> Unit) {
    val username by viewModel.username.collectAsState(initial = "")
    val password by viewModel.password.collectAsState(initial = "")
    val rememberMe by viewModel.rememberMe.collectAsState(initial = false)
    val hasUserAcceptedTerms by viewModel.hasUserAcceptedTerms.collectAsState(initial = false)
    val isLoginButtonEnabled by viewModel.loginButtonEnabled.collectAsState(initial = false)

    var displayLoginFailedDialog by rememberSaveable { mutableStateOf(false) }
    var displayPerformLoginDialog by rememberSaveable { mutableStateOf(false) }

    val serverConfigurationService: ServerConfigurationService = get()
    val serverDataService: ServerDataService = get()

    @Suppress("MoveVariableDeclarationIntoWhen")
    val profileInfo =
        serverConfigurationService
            .serverUrl
            .transformLatest { serverUrl ->
                emitAll(serverDataService.getServerProfileInfo(serverUrl))
            }
            .collectAsState(initial = DataState.Loading()).value

    val accountName = fromProfileInfo(profileInfo, "") { it.accountName ?: "" }

    val needsToAcceptTerms = fromProfileInfo(profileInfo, false) { it.needsToAcceptTerms }

    val isPasswordLoginDisabled = fromProfileInfo(profileInfo, false) { it.isPasswordLoginDisabled }

    val saml2Config: Saml2Config? = fromProfileInfo(profileInfo, null) { it.saml2 }

    var loginJob: Job? by remember { mutableStateOf(null) }

    LoginUi(
        modifier = modifier,
        username = username,
        password = password,
        rememberMe = rememberMe,
        hasUserAcceptedTerms = hasUserAcceptedTerms,
        updateUsername = viewModel::updateUsername,
        updatePassword = viewModel::updatePassword,
        updateRememberMe = viewModel::updateRememberMe,
        updateUserAcceptedTerms = viewModel::updateUserAcceptedTerms,
        onClickLogin = {
            displayPerformLoginDialog = true

            loginJob = viewModel.login(
                onSuccess = {
                    displayPerformLoginDialog = false
                    onLoggedIn()
                },
                onFailure = {
                    displayPerformLoginDialog = false
                    displayLoginFailedDialog = true
                }
            )
        },
        isLoginButtonEnabled = isLoginButtonEnabled,
        accountName = accountName,
        needsToAcceptTerms = needsToAcceptTerms,
        isPasswordLoginDisabled = isPasswordLoginDisabled,
        saml2Config = saml2Config
    )

    if (displayPerformLoginDialog) {
        AlertDialog(
            title = {
                Text(text = stringResource(id = R.string.login_dialog_perform_login_title))
            },
            onDismissRequest = {
                try {
                    loginJob?.cancel()
                    loginJob = null
                    displayPerformLoginDialog = false
                } catch (_: Exception) {
                }
            },
            confirmButton = {}
        )
    }

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
 * The user can trigger .
 *
 * @param accountName the account name from [de.tum.informatics.www1.artemis.native_app.core.server_config.ProfileInfo.accountName]
 * @param onClickLogin called when the user clicks the login button.
 * @param isLoginButtonEnabled if the button that sends the login request is enabled.
 * @param isPasswordLoginDisabled see [de.tum.informatics.www1.artemis.native_app.core.server_config.ProfileInfo.isPasswordLoginDisabled]
 * @param saml2Config see [de.tum.informatics.www1.artemis.native_app.core.server_config.ProfileInfo.saml2]
 */
@Composable
fun LoginUi(
    modifier: Modifier,
    username: String,
    password: String,
    rememberMe: Boolean,
    hasUserAcceptedTerms: Boolean,
    updateUsername: (String) -> Unit,
    updatePassword: (String) -> Unit,
    updateRememberMe: (Boolean) -> Unit,
    updateUserAcceptedTerms: (Boolean) -> Unit,
    onClickLogin: () -> Unit,
    isLoginButtonEnabled: Boolean,
    accountName: String,
    needsToAcceptTerms: Boolean,
    isPasswordLoginDisabled: Boolean,
    saml2Config: Saml2Config?
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

        val loginModifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(0.8f)
            .widthIn(max = 600.dp)
            .align(Alignment.CenterHorizontally)

        if (!isPasswordLoginDisabled) {
            PasswordBasedLogin(
                modifier = loginModifier,
                username = username,
                updateUsername = updateUsername,
                password = password,
                updatePassword = updatePassword,
                rememberMe = rememberMe,
                updateRememberMe = updateRememberMe,
                isLoginButtonEnabled = isLoginButtonEnabled,
                onClickLogin = onClickLogin
            )
        }

        if (!isPasswordLoginDisabled && saml2Config != null) {
            //Both are visible, therefore we place a visual divider
            DividerWithText(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = { modifier ->
                    Text(
                        modifier = modifier,
                        text = stringResource(id = R.string.login_password_or_saml_divider_text),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            )
        }

        if (saml2Config != null) {
            Saml2BasedLogin(
                modifier = loginModifier,
                saml2Config = saml2Config,
                passwordLoginDisabled = isPasswordLoginDisabled,
                needsToAcceptTerms = needsToAcceptTerms,
                hasUserAcceptedTerms = hasUserAcceptedTerms,
                rememberMe = rememberMe,
                updateRememberMe = updateRememberMe,
                onLoginButtonClicked = {

                }
            )
        }

        if (needsToAcceptTerms) {
            CheckboxWithText(
                modifier = loginModifier,
                isChecked = hasUserAcceptedTerms,
                text = stringResource(id = R.string.login_remember_me_label),
                onCheckedChanged = updateUserAcceptedTerms
            )
        }
    }
}

@Composable
private fun PasswordBasedLogin(
    modifier: Modifier,
    username: String,
    password: String,
    rememberMe: Boolean,
    updateUsername: (String) -> Unit,
    updatePassword: (String) -> Unit,
    updateRememberMe: (Boolean) -> Unit,
    isLoginButtonEnabled: Boolean,
    onClickLogin: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = username,
            onValueChange = updateUsername,
            label = { Text(text = stringResource(id = R.string.login_username_label)) }
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = password,
            onValueChange = updatePassword,
            label = { Text(text = stringResource(id = R.string.login_password_label)) },
            visualTransformation = remember { PasswordVisualTransformation() }
        )

        RememberLoginCheckBox(
            modifier = Modifier.fillMaxWidth(),
            rememberMe = rememberMe,
            updateRememberMe = updateRememberMe
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClickLogin,
            enabled = isLoginButtonEnabled
        ) {
            Text(text = stringResource(id = R.string.login_perform_login_button_text))
        }
    }
}

@Composable
private fun Saml2BasedLogin(
    modifier: Modifier,
    saml2Config: Saml2Config,
    passwordLoginDisabled: Boolean,
    needsToAcceptTerms: Boolean,
    hasUserAcceptedTerms: Boolean,
    rememberMe: Boolean,
    updateRememberMe: (Boolean) -> Unit,
    onLoginButtonClicked: () -> Unit
) {
    val elementModifier = Modifier.fillMaxWidth()

    Column(modifier = modifier) {
        val identityProviderName = saml2Config.identityProviderName
        val pleaseSignInText = if (identityProviderName != null) {
            stringResource(
                id = R.string.login_saml_please_sign_in_provider,
                identityProviderName
            )
        } else stringResource(id = R.string.login_saml_please_sign_in)

        if (!passwordLoginDisabled) {
            Text(
                modifier = elementModifier,
                text = pleaseSignInText,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }

        RememberLoginCheckBox(
            modifier = elementModifier,
            rememberMe = rememberMe,
            updateRememberMe = updateRememberMe
        )

        Button(
            modifier = elementModifier,
            onClick = onLoginButtonClicked,
            enabled = !needsToAcceptTerms || hasUserAcceptedTerms,
            content = {
                Text(
                    text = saml2Config.buttonLabel
                        ?: stringResource(id = R.string.login_saml_button_label)
                )
            }
        )

        if (needsToAcceptTerms && !hasUserAcceptedTerms) {
            Text(
                modifier = elementModifier,
                text = stringResource(id = R.string.login_error_accept_terms),
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RememberLoginCheckBox(
    modifier: Modifier,
    rememberMe: Boolean,
    updateRememberMe: (Boolean) -> Unit
) {
    CheckboxWithText(
        modifier = modifier,
        isChecked = rememberMe,
        text = stringResource(id = R.string.login_remember_me_label),
        onCheckedChanged = updateRememberMe
    )
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

@Composable
private fun DividerWithText(modifier: Modifier, text: @Composable (Modifier) -> Unit) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Divider(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )

        text(Modifier)

        Divider(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )
    }
}