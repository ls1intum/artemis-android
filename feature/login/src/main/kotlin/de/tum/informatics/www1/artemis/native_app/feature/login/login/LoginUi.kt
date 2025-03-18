package de.tum.informatics.www1.artemis.native_app.feature.login.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.server_config.ProfileInfo
import de.tum.informatics.www1.artemis.native_app.core.model.server_config.Saml2Config
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalLinkOpener
import de.tum.informatics.www1.artemis.native_app.feature.login.R
import de.tum.informatics.www1.artemis.native_app.feature.login.login.login_options.PasswordBasedLogin
import de.tum.informatics.www1.artemis.native_app.feature.login.login.login_options.Saml2BasedLogin
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.Deferred

/*
 * Autofill code is taken and inspired by https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/ui/ui/integration-tests/ui-demos/src/main/java/androidx/compose/ui/demos/autofill/ExplicitAutofillTypesDemo.kt
 * https://cs.android.com/androidx/platform/frameworks/support
 * */

@Composable
internal fun LoginScreen(
    modifier: Modifier,
    viewModel: LoginViewModel,
    onLoggedIn: () -> Unit,
    onClickSaml2Login: (rememberMe: Boolean) -> Unit
) {
    Box(
        modifier = modifier
    ) {
        LoginUi(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            viewModel = viewModel,
            onLoggedIn = onLoggedIn,
            onClickSaml2Login = onClickSaml2Login
        )
    }
}

@Composable
internal fun LoginUi(
    modifier: Modifier,
    viewModel: LoginViewModel,
    onLoggedIn: () -> Unit,
    onClickSaml2Login: (rememberMe: Boolean) -> Unit
) {
    val linkOpener = LocalLinkOpener.current

    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val rememberMe by viewModel.rememberMe.collectAsState()
    val hasUserAcceptedTerms by viewModel.hasUserAcceptedTerms.collectAsState()
    val isLoginButtonEnabled by viewModel.loginButtonEnabled.collectAsState()

    val serverUrl: String by viewModel.serverUrl.collectAsState()

    var displayLoginFailedDialog by rememberSaveable { mutableStateOf(false) }

    val profileInfo = viewModel.serverProfileInfo.collectAsState().value

    val accountName = fromProfileInfo(profileInfo, "") { it.accountName.orEmpty() }

    val needsToAcceptTerms = fromProfileInfo(profileInfo, false) { it.needsToAcceptTerms }

    val isPasswordLoginDisabled = fromProfileInfo(profileInfo, false) { it.isPasswordLoginDisabled }

    val saml2Config: Saml2Config? = fromProfileInfo(profileInfo, null) { it.saml2 }

    var loginJob: Deferred<Boolean>? by remember { mutableStateOf(null) }

    AwaitDeferredCompletion(job = loginJob) { wasSuccessful ->
        loginJob = null

        if (wasSuccessful) {
            onLoggedIn()
        } else {
            displayLoginFailedDialog = true
        }
    }

    LoginUi(
        modifier = modifier,
        accountName = accountName,
        needsToAcceptTerms = needsToAcceptTerms,
        hasUserAcceptedTerms = hasUserAcceptedTerms,
        saml2Config = saml2Config,
        isPasswordLoginDisabled = isPasswordLoginDisabled,
        updateUserAcceptedTerms = viewModel::updateUserAcceptedTerms,
        passwordBasedLoginContent = { loginModifier ->
            PasswordBasedLogin(
                modifier = loginModifier,
                username = username,
                password = password,
                rememberMe = rememberMe,
                isLoggingIn = loginJob != null,
                updateUsername = viewModel::updateUsername,
                updatePassword = viewModel::updatePassword,
                updateRememberMe = viewModel::updateRememberMe,
                isLoginButtonEnabled = isLoginButtonEnabled,
                onClickLogin = {
                    loginJob = viewModel.login()
                },
                onClickForgotPassword = {
                    val link = URLBuilder(serverUrl)
                        .appendPathSegments("account", "reset", "request")
                        .buildString()

                    linkOpener.openLink(link)
                }
            )
        },
        saml2BasedLoginContent = { loginModifier, providedSaml2Config ->
            Saml2BasedLogin(
                modifier = loginModifier,
                saml2Config = providedSaml2Config,
                passwordLoginDisabled = isPasswordLoginDisabled,
                needsToAcceptTerms = needsToAcceptTerms,
                hasUserAcceptedTerms = hasUserAcceptedTerms,
                rememberMe = rememberMe,
                updateRememberMe = viewModel::updateRememberMe,
                onLoginButtonClicked = { onClickSaml2Login(rememberMe) }
            )
        }
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
 * The user can trigger .
 *
 * @param accountName the account name from [ProfileInfo.accountName]
 * @param isPasswordLoginDisabled see [ProfileInfo.isPasswordLoginDisabled]
 * @param saml2Config see [ProfileInfo.saml2]
 */
@Composable
internal fun LoginUi(
    modifier: Modifier,
    accountName: String,
    needsToAcceptTerms: Boolean,
    hasUserAcceptedTerms: Boolean,
    saml2Config: Saml2Config?,
    isPasswordLoginDisabled: Boolean,
    updateUserAcceptedTerms: (Boolean) -> Unit,
    passwordBasedLoginContent: @Composable (modifier: Modifier) -> Unit,
    saml2BasedLoginContent: @Composable (modifier: Modifier, config: Saml2Config) -> Unit
) {
    Column(modifier = modifier.then(Modifier.verticalScroll(rememberScrollState()))) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            text = stringResource(id = R.string.login_please_sign_in_account, accountName),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        val loginModifier = Modifier
            .padding(vertical = 16.dp)
            .widthIn(max = 600.dp)
            .fillMaxWidth(0.8f)
            .align(Alignment.CenterHorizontally)

        if (!isPasswordLoginDisabled) {
            passwordBasedLoginContent(loginModifier)
        }

        if (!isPasswordLoginDisabled && saml2Config != null) {
            //Both are visible, therefore we place a visual divider
            DividerWithText(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = { modifier ->
                    Text(
                        modifier = modifier,
                        text = stringResource(id = R.string.login_password_or_saml_divider_text),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,)
                }
            )
        }

        if (saml2Config != null) {
            saml2BasedLoginContent(loginModifier, saml2Config)
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

private fun <T> fromProfileInfo(
    dataState: DataState<ProfileInfo>,
    default: T,
    onSuccess: (ProfileInfo) -> T
): T = dataState.bind(onSuccess).orElse(default)

@Composable
@Preview
fun LoginUiPleaseSignInPreview() {
    LoginUi(
        modifier = Modifier,
        accountName = "Artemis",
        needsToAcceptTerms = false,
        hasUserAcceptedTerms = true,
        saml2Config = null,
        isPasswordLoginDisabled = false,
        updateUserAcceptedTerms = {},
        passwordBasedLoginContent = { },
        saml2BasedLoginContent = { _, _ -> }
    )
}
