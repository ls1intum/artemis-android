package de.tum.informatics.www1.artemis.native_app.android.ui.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.tum.informatics.www1.artemis.native_app.android.R
import de.tum.informatics.www1.artemis.native_app.android.server_config.ProfileInfo
import de.tum.informatics.www1.artemis.native_app.android.defaults.ArtemisInstances
import de.tum.informatics.www1.artemis.native_app.android.ui.account.login.LoginUi
import de.tum.informatics.www1.artemis.native_app.android.ui.account.login.LoginViewModel
import de.tum.informatics.www1.artemis.native_app.android.util.DataState
import io.ktor.http.*
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf
import java.io.IOException

/**
 * Displays the screen to login and register. Also allows to change the artemis instance.
 */
@Composable
fun AccountScreen(
    modifier: Modifier,
    viewModel: AccountViewModel = getViewModel(),
    onNavigateToLoginScreen: () -> Unit,
    onNavigateToRegisterScreen: () -> Unit,
    onLoggedIn: () -> Unit
) {
    val artemisInstance by viewModel.selectedArtemisInstance
        .collectAsState(initial = ArtemisInstances.TUM_ARTEMIS)

    val serverProfileInfo by viewModel.serverProfileInfo.collectAsState(initial = DataState.Suspended())

    AccountUi(
        modifier = modifier,
        artemisInstance = artemisInstance,
        updateServerUrl = viewModel::updateServerUrl,
        serverProfileInfo = serverProfileInfo,
        retryLoadServerProfileInfo = viewModel::retryLoadServerProfileInfo,
        onLoggedIn = onLoggedIn,
        onNavigateToLoginScreen = onNavigateToLoginScreen,
        onNavigateToRegisterScreen = onNavigateToRegisterScreen
    )
}

@Composable
private fun AccountUi(
    modifier: Modifier,
    artemisInstance: ArtemisInstances.ArtemisInstance,
    serverProfileInfo: DataState<ProfileInfo>,
    updateServerUrl: (String) -> Unit,
    retryLoadServerProfileInfo: () -> Unit,
    onNavigateToLoginScreen: () -> Unit,
    onNavigateToRegisterScreen: () -> Unit,
    onLoggedIn: () -> Unit
) {
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.15f)
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.account_screen_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                text = stringResource(id = R.string.account_screen_subtitle),
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.05f)
            )

            ArtemisInstanceSelection(
                modifier = Modifier
                    .fillMaxWidth(1.0f)
                    .padding(horizontal = 16.dp)
                    .align(Alignment.CenterHorizontally),
                artemisInstance = artemisInstance,
                changeUrl = updateServerUrl
            )

            RegisterLoginAccount(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                serverProfileInfo = serverProfileInfo,
                retryLoadServerProfileInfo = retryLoadServerProfileInfo,
                onLoggedIn = onLoggedIn,
                onNavigateToLoginScreen = onNavigateToLoginScreen,
                onNavigateToRegisterScreen = onNavigateToRegisterScreen
            )
        }
    }
}

@Composable
fun ArtemisInstanceSelection(
    modifier: Modifier,
    artemisInstance: ArtemisInstances.ArtemisInstance,
    changeUrl: (String) -> Unit
) {
    var dropdownMenuDisplayed: Boolean by rememberSaveable {
        mutableStateOf(false)
    }

    Column(modifier = modifier) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.account_select_artemis_instance_select_text),
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                ArtemisInstance(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    name = stringResource(id = artemisInstance.name),
                    icon = null
                )

                Box {
                    IconButton(
                        modifier = Modifier,
                        onClick = { dropdownMenuDisplayed = true }
                    ) {
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        expanded = dropdownMenuDisplayed,
                        onDismissRequest = { dropdownMenuDisplayed = false }) {
                        ArtemisInstances.instances.forEach { instance ->
                            DropdownMenuItem(
                                text = {
                                    ArtemisInstance(
                                        modifier = Modifier.fillMaxWidth(),
                                        name = stringResource(id = instance.name),
                                        icon = null
                                    )
                                }, onClick = {
                                    changeUrl(instance.serverUrl)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtemisInstance(modifier: Modifier, name: String, icon: Painter?) {
    Row(modifier = modifier.then(Modifier.height(IntrinsicSize.Min))) {
        val iconModifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
        if (icon != null) {
            Image(modifier = iconModifier, painter = icon, contentDescription = null)
        } else {
            Box(modifier = iconModifier)
        }

        Text(
            modifier = Modifier.weight(9f),
            text = name,
            fontSize = 20.sp
        )
    }
}

/**
 * Displays the server info data state.
 * If the profile info is loaded, it displays the following:
 * If the server supports registration, then a two buttons are displays for internal navigation to login/register
 * Otherwise, only the login is displayed.
 */
@Composable
private fun RegisterLoginAccount(
    modifier: Modifier,
    serverProfileInfo: DataState<ProfileInfo>,
    retryLoadServerProfileInfo: () -> Unit,
    onNavigateToLoginScreen: () -> Unit,
    onNavigateToRegisterScreen: () -> Unit,
    onLoggedIn: () -> Unit
) {
    Box(
        modifier = modifier
    ) {
        val columnModifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth(0.8f)

        when (serverProfileInfo) {
            is DataState.Failure, is DataState.Suspended -> {
                Column(
                    modifier = columnModifier
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(
                            when (serverProfileInfo) {
                                is DataState.Failure -> R.string.account_load_server_profile_failure
                                is DataState.Suspended -> R.string.account_load_server_profile_suspended
                                else -> 0 //Not reachable
                            }
                        ),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp
                    )

                    TextButton(
                        onClick = retryLoadServerProfileInfo,
                        content = { Text(text = stringResource(id = R.string.account_load_server_profile_button_try_again)) },
                        modifier = Modifier
                            .padding(top = 0.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
            is DataState.Loading -> {
                Column(modifier = columnModifier) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.account_load_server_profile_loading),
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                    )

                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 4.dp)
                    )
                }
            }
            is DataState.Success -> {
                if (serverProfileInfo.data.registrationEnabled == true) {
                    LoginOrRegister(
                        modifier = Modifier.fillMaxSize(),
                        onClickLogin = onNavigateToLoginScreen,
                        onClickRegister = onNavigateToRegisterScreen
                    )
                } else {
                    val loginUiModifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .align(Alignment.Center)

                    if (LocalInspectionMode.current) {
                        //Just for the preview.
                        LoginUi(
                            modifier = loginUiModifier,
                            username = "",
                            password = "",
                            userAcceptedTerms = false,
                            rememberMe = false,
                            updateUsername = {},
                            updatePassword = {},
                            updateRememberMe = {},
                            updateUserAcceptedTerms = {},
                            onClickLogin = {},
                            isLoginEnabled = false,
                            accountName = "TUM",
                            needsToAcceptTerms = false
                        )
                    } else {
                        LoginUi(
                            modifier = loginUiModifier,
                            viewModel = getViewModel(parameters = { parametersOf(serverProfileInfo.data) }),
                            onLoggedIn = onLoggedIn
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginOrRegister(
    modifier: Modifier,
    onClickLogin: () -> Unit,
    onClickRegister: () -> Unit
) {
    @Suppress("LocalVariableName")
    val Label = @Composable { text: String, padding: PaddingValues ->
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            text = text,
            textAlign = TextAlign.Center,
            fontSize = 22.sp
        )
    }


    Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
        @Suppress("LocalVariableName")
        val Button = @Composable { text: String, onClick: () -> Unit ->
            Button(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.6f),
                onClick = onClick
            ) {
                Text(
                    text = text,
                    fontSize = 22.sp
                )
            }
        }

        Label(
            stringResource(id = R.string.account_login_register_selection_login_text),
            PaddingValues(bottom = 8.dp)
        )

        Button(
            stringResource(id = R.string.account_login_register_selection_login_button),
            onClickLogin
        )

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 24.dp, bottom = 8.dp)
        )

        Label(
            stringResource(id = R.string.account_login_register_selection_register_text),
            PaddingValues(top = 8.dp, bottom = 8.dp)
        )

        Button(
            stringResource(id = R.string.account_login_register_selection_register_button),
            onClickRegister
        )
    }
}

@Composable
@Preview(name = "LOADING: profile info")
fun AccountUiPreviewLoadingProfileInfo() {
    AccountUi(
        modifier = Modifier.fillMaxSize(),
        artemisInstance = ArtemisInstances.TUM_ARTEMIS,
        serverProfileInfo = DataState.Loading(),
        updateServerUrl = {},
        retryLoadServerProfileInfo = {},
        onLoggedIn = {},
        onNavigateToLoginScreen = {},
        onNavigateToRegisterScreen = {}
    )
}

@Composable
@Preview(name = "FAILED: profile info")
fun AccountUiPreviewFailedLoadingProfileInfo() {
    AccountUi(
        modifier = Modifier.fillMaxSize(),
        artemisInstance = ArtemisInstances.TUM_ARTEMIS,
        serverProfileInfo = DataState.Failure(IOException()),
        updateServerUrl = {},
        retryLoadServerProfileInfo = {},
        onLoggedIn = {},
        onNavigateToLoginScreen = {},
        onNavigateToRegisterScreen = {}
    )
}

@Composable
@Preview(name = "SUSPENDED: profile info")
fun AccountUiPreviewSuspendedLoadingProfileInfo() {
    AccountUi(
        modifier = Modifier.fillMaxSize(),
        artemisInstance = ArtemisInstances.TUM_ARTEMIS,
        serverProfileInfo = DataState.Suspended(null),
        updateServerUrl = {},
        retryLoadServerProfileInfo = {},
        onLoggedIn = {},
        onNavigateToLoginScreen = {},
        onNavigateToRegisterScreen = {}
    )
}

@Composable
@Preview(name = "Registration enabled")
fun AccountUiPreviewWithRegister() {
    AccountUi(
        modifier = Modifier.fillMaxSize(),
        artemisInstance = ArtemisInstances.TUM_ARTEMIS,
        serverProfileInfo = DataState.Success(
            ProfileInfo(
                registrationEnabled = true
            )
        ),
        updateServerUrl = {},
        retryLoadServerProfileInfo = {},
        onLoggedIn = {},
        onNavigateToLoginScreen = {},
        onNavigateToRegisterScreen = {}
    )
}

@Composable
@Preview(name = "Registration disabled")
fun AccountUiPreviewWithoutRegister() {
    AccountUi(
        modifier = Modifier.fillMaxSize(),
        artemisInstance = ArtemisInstances.TUM_ARTEMIS,
        serverProfileInfo = DataState.Success(
            ProfileInfo(
                registrationEnabled = false
            )
        ),
        updateServerUrl = {},
        retryLoadServerProfileInfo = {},
        onLoggedIn = {},
        onNavigateToLoginScreen = {},
        onNavigateToRegisterScreen = {}
    )
}

@Composable
@Preview(widthDp = 200)
fun LoginOrRegisterPreview() {
    LoginOrRegister(
        modifier = Modifier.fillMaxWidth(),
        onClickLogin = {},
        onClickRegister = {}
    )
}