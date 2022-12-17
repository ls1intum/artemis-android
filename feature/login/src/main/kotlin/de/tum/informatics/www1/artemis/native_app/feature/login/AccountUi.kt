package de.tum.informatics.www1.artemis.native_app.feature.login

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import de.tum.informatics.www1.artemis.native_app.core.model.server_config.ProfileInfo
import de.tum.informatics.www1.artemis.native_app.core.datastore.defaults.ArtemisInstances
import de.tum.informatics.www1.artemis.native_app.feature.login.login.LoginUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.feature.account.R
import de.tum.informatics.www1.artemis.native_app.feature.login.login.LoginScreen
import org.koin.androidx.compose.getStateViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.compose.koinViewModel
import java.io.IOException

const val LOGIN_DESTINATION = "login"

private const val NESTED_HOME_DESTINATION = "nested_home"
private const val NESTED_LOGIN_DESTINATION = "nested_login"
private const val NESTED_REGISTER_DESTINATION = "nested_register"

fun NavController.navigateToLogin(builder: NavOptionsBuilder.() -> Unit) {
    navigate(LOGIN_DESTINATION, builder)
}

fun NavGraphBuilder.loginScreen(
    onLoggedIn: () -> Unit
) {
    composable(LOGIN_DESTINATION) {
        val nestedNavController = rememberNavController()

        NavHost(navController = nestedNavController, startDestination = NESTED_HOME_DESTINATION) {
            composable(NESTED_HOME_DESTINATION) {
                AccountScreen(
                    modifier = Modifier.fillMaxSize(),
                    onNavigateToLoginScreen = {
                        nestedNavController.navigate(NESTED_LOGIN_DESTINATION)
                    },
                    onNavigateToRegisterScreen = {
                        nestedNavController.navigate(NESTED_REGISTER_DESTINATION)
                    },
                    onLoggedIn = onLoggedIn
                )
            }

            composable(NESTED_LOGIN_DESTINATION) {
                LoginScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = getViewModel(),
                    onLoggedIn = onLoggedIn
                )
            }

            composable(NESTED_REGISTER_DESTINATION) {
//                RegisterUi(
//                    modifier = Modifier.fillMaxSize(),
//                    viewModel =
//                )
            }
        }
    }
}

/**
 * Displays the screen to login and register. Also allows to change the artemis instance.
 */
@Composable
internal fun AccountScreen(
    modifier: Modifier,
    viewModel: AccountViewModel = koinViewModel(),
    onNavigateToLoginScreen: () -> Unit,
    onNavigateToRegisterScreen: () -> Unit,
    onLoggedIn: () -> Unit
) {
    val artemisInstance by viewModel.selectedArtemisInstance.collectAsState()

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
            ArtemisInstanceSelection(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(70.dp)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .align(Alignment.End),
                artemisInstance = artemisInstance,
                changeUrl = updateServerUrl
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.05f)
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

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            modifier = Modifier.weight(2f),
            text = stringResource(id = R.string.account_select_artemis_instance_select_text),
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )

        Card(
            modifier = Modifier
                .weight(1f),
            onClick = { dropdownMenuDisplayed = true },
            border = BorderStroke(2.dp, color = MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .weight(4f)
                        .align(Alignment.CenterVertically)
                        .padding(start = 4.dp)
                ) {
                    ArtemisInstanceLogo(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(vertical = 4.dp),
                        serverUrl = artemisInstance.serverUrl
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                )

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
                                    serverUrl = instance.serverUrl
                                )
                            }, onClick = {
                                changeUrl(instance.serverUrl)
                                dropdownMenuDisplayed = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtemisInstance(modifier: Modifier, name: String, serverUrl: String) {
    Row(
        modifier = modifier.then(Modifier.height(IntrinsicSize.Min)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ArtemisInstanceLogo(modifier = Modifier.weight(1f), serverUrl = serverUrl)

        Text(
            modifier = Modifier.weight(2f),
            text = name,
            fontSize = 16.sp
        )
    }
}

/**
 * Displays the public/images/logo.png of the given artemis instance
 */
@Composable
private fun ArtemisInstanceLogo(modifier: Modifier, serverUrl: String) {
    val model = ImageRequest
        .Builder(LocalContext.current)
        .data("${serverUrl}public/images/logo.png")
        .build()

    AsyncImage(
        modifier = modifier,
        model = model,
        contentScale = ContentScale.Fit,
        contentDescription = null,
        placeholder = rememberVectorPainter(image = Icons.Default.Downloading)
    )
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
    Crossfade(
        modifier = modifier,
        targetState = serverProfileInfo,
        animationSpec = tween(50)
    ) { currentServerProfileInfo ->
        Box(modifier = Modifier.fillMaxWidth()) {
            val columnModifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.8f)

            BasicDataStateUi(
                modifier = Modifier.fillMaxWidth(),
                dataState = currentServerProfileInfo,
                loadingText = stringResource(id = R.string.account_load_server_profile_loading),
                failureText = stringResource(id = R.string.account_load_server_profile_failure),
                suspendedText = stringResource(id = R.string.account_load_server_profile_suspended),
                retryButtonText = stringResource(id = R.string.account_load_server_profile_button_try_again),
                onClickRetry = retryLoadServerProfileInfo
            ) { data ->

                if (data.registrationEnabled == true) {
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
                            hasUserAcceptedTerms = false,
                            rememberMe = false,
                            updateUsername = {},
                            updatePassword = {},
                            updateRememberMe = {},
                            updateUserAcceptedTerms = {},
                            onClickLogin = {},
                            isLoginButtonEnabled = false,
                            accountName = "TUM",
                            needsToAcceptTerms = false,
                            isPasswordLoginDisabled = false,
                            saml2Config = null
                        )
                    } else {
                        LoginUi(
                            modifier = loginUiModifier,
                            viewModel = getStateViewModel(),
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