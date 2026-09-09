package de.tum.informatics.www1.artemis.native_app.feature.login.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.common.ActiveModuleFeature
import de.tum.informatics.www1.artemis.native_app.core.common.FeatureAvailability
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.server_config.ProfileInfo
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalLinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.common.ButtonWithLoadingAnimation
import de.tum.informatics.www1.artemis.native_app.feature.login.R
import de.tum.informatics.www1.artemis.native_app.feature.login.login.login_options.OidcBasedLogin
import de.tum.informatics.www1.artemis.native_app.feature.login.login.login_options.PasskeyBasedLogin
import de.tum.informatics.www1.artemis.native_app.feature.login.login.login_options.Saml2BasedLogin
import de.tum.informatics.www1.artemis.native_app.feature.login.service.LoginMethod
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
    Box(modifier = modifier) {
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
    val authPhase by viewModel.authPhase.collectAsState()
    val loginOptions by viewModel.loginOptions.collectAsState()
    val singleSSOOption by viewModel.singleSSOOption.collectAsState()
    val isLoginButtonEnabled by viewModel.loginButtonEnabled.collectAsState()
    val isContinueButtonEnabled by viewModel.continueButtonEnabled.collectAsState()
    val serverUrl: String by viewModel.serverUrl.collectAsState()

    var displayLoginFailedDialog by rememberSaveable { mutableStateOf(false) }

    val profileInfo = viewModel.serverProfileInfo.collectAsState().value
    val accountName = fromProfileInfo(profileInfo, "") { it.accountName.orEmpty() }

    var fetchOptionsJob: Deferred<Boolean>? by remember { mutableStateOf(null) }
    var loginJob: Deferred<Boolean>? by remember { mutableStateOf(null) }
    var loginWithPasskeyJob: Deferred<Boolean>? by remember { mutableStateOf(null) }

    AwaitDeferredCompletion(job = fetchOptionsJob) { wasSuccessful ->
        fetchOptionsJob = null
        if (!wasSuccessful) {
            displayLoginFailedDialog = true
        }
    }

    AwaitDeferredCompletion(job = loginJob) { wasSuccessful ->
        loginJob = null
        if (wasSuccessful) {
            onLoggedIn()
        } else {
            displayLoginFailedDialog = true
        }
    }

    AwaitDeferredCompletion(job = loginWithPasskeyJob) { wasSuccessful ->
        loginWithPasskeyJob = null
        if (wasSuccessful) {
            onLoggedIn()
        } else {
            displayLoginFailedDialog = true
        }
    }

    Column(
        modifier = modifier.then(Modifier.verticalScroll(rememberScrollState()))
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            text = stringResource(id = R.string.login_please_sign_in_account, accountName),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        val formModifier = Modifier
            .padding(vertical = 16.dp)
            .widthIn(max = 600.dp)
            .fillMaxWidth(0.8f)
            .align(Alignment.CenterHorizontally)

        Column(
            modifier = formModifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (singleSSOOption == null) {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = username,
                    onValueChange = viewModel::updateUsername,
                    enabled = authPhase == AuthPhase.USERNAME,
                    label = { Text(text = stringResource(id = R.string.login_username_label)) },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = if (authPhase == AuthPhase.USERNAME) ImeAction.Next else ImeAction.None
                    )
                )
            }

            if (authPhase == AuthPhase.USERNAME) {
                ButtonWithLoadingAnimation(
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = fetchOptionsJob != null,
                    enabled = isContinueButtonEnabled && fetchOptionsJob == null,
                    onClick = { fetchOptionsJob = viewModel.fetchLoginOptions() }
                ) {
                    Text(text = "Continue")
                }

                if (FeatureAvailability.isEnabled(ActiveModuleFeature.Passkey)) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    PasskeyBasedLogin(
                        modifier = Modifier.fillMaxWidth(),
                        onPasskeyLogin = { loginWithPasskeyJob = viewModel.loginWithPasskey() },
                        isLoggingIn = loginWithPasskeyJob != null,
                        isEnabled = fetchOptionsJob == null
                    )
                }
            }

            if (authPhase == AuthPhase.CREDENTIALS) {
                when (loginOptions?.loginMethod) {
                    LoginMethod.PASSWORD -> {
                        PasswordTextField(
                            modifier = Modifier.fillMaxWidth(),
                            password = password,
                            updatePassword = viewModel::updatePassword,
                            label = stringResource(id = R.string.login_password_label)
                        )

                        RememberLoginCheckBox(
                            modifier = Modifier.fillMaxWidth(),
                            rememberMe = rememberMe,
                            updateRememberMe = viewModel::updateRememberMe
                        )

                        ButtonWithLoadingAnimation(
                            modifier = Modifier.fillMaxWidth(),
                            isLoading = loginJob != null,
                            onClick = { loginJob = viewModel.login() },
                            enabled = isLoginButtonEnabled && loginJob == null
                        ) {
                            Text(text = stringResource(id = R.string.login_perform_login_button_text))
                        }

                        TextButton(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            onClick = {
                                val link = URLBuilder(serverUrl)
                                    .appendPathSegments("account", "reset", "request")
                                    .buildString()
                                linkOpener.openLink(link)
                            }
                        ) {
                            Text(text = stringResource(id = R.string.login_password_forgot))
                        }
                    }

                    LoginMethod.SAML2 -> {
                        val saml2Config = (profileInfo as? DataState.Success)?.data?.saml2
                        if (saml2Config != null) {
                            Saml2BasedLogin(
                                modifier = Modifier.fillMaxWidth(),
                                saml2Config = saml2Config,
                                passwordLoginDisabled = true,
                                needsToAcceptTerms = false,
                                hasUserAcceptedTerms = true,
                                rememberMe = rememberMe,
                                updateRememberMe = viewModel::updateRememberMe,
                                onLoginButtonClicked = { onClickSaml2Login(rememberMe) }
                            )
                        }
                    }

                    LoginMethod.OIDC -> {
                        OidcBasedLogin(
                            modifier = Modifier.fillMaxWidth(),
                            idpName = loginOptions?.idpName,
                            rememberMe = rememberMe,
                            updateRememberMe = viewModel::updateRememberMe,
                            onLoginButtonClicked = {
                                println("Clicked OIDC login!")
                            }
                        )
                    }

                    null -> {}
                }

                if (singleSSOOption == null) {
                    TextButton(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = viewModel::resetToUsernamePhase
                    ) {
                        Text(text = "← Back")
                    }
                }
            }
        }
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

private fun <T> fromProfileInfo(
    dataState: DataState<ProfileInfo>,
    default: T,
    onSuccess: (ProfileInfo) -> T
): T = dataState.bind(onSuccess).orElse(default)