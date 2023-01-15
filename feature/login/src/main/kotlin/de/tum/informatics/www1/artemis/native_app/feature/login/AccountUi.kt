package de.tum.informatics.www1.artemis.native_app.feature.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
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
import de.tum.informatics.www1.artemis.native_app.core.model.server_config.ProfileInfo
import de.tum.informatics.www1.artemis.native_app.core.datastore.defaults.ArtemisInstances
import de.tum.informatics.www1.artemis.native_app.feature.login.login.LoginUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.account.R
import de.tum.informatics.www1.artemis.native_app.feature.login.custom_instance_selection.CustomInstanceSelectionScreen
import de.tum.informatics.www1.artemis.native_app.feature.login.instance_selection.InstanceSelectionScreen
import de.tum.informatics.www1.artemis.native_app.feature.login.login.LoginScreen
import de.tum.informatics.www1.artemis.native_app.feature.login.service.ServerNotificationStorageService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getStateViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.compose.koinViewModel
import java.io.IOException

const val LOGIN_DESTINATION = "login"

private const val NESTED_INSTANCE_SELECTION_DESTINATION = "instance_selection"
private const val NESTED_CUSTOM_INSTANCE_SELECTION_DESTINATION = "custom_instance_selection"
private const val NESTED_HOME_DESTINATION = "nested_home"
private const val NESTED_LOGIN_DESTINATION = "nested_login"
private const val NESTED_REGISTER_DESTINATION = "nested_register"

fun NavController.navigateToLogin(builder: NavOptionsBuilder.() -> Unit) {
    navigate(LOGIN_DESTINATION, builder)
}

fun NavGraphBuilder.loginScreen(
    onFinishedLoginFlow: () -> Unit,
    onRequestOpenSettings: () -> Unit
) {
    composable(LOGIN_DESTINATION) {
        val scope = rememberCoroutineScope()
        val serverNotificationStorageService: ServerNotificationStorageService = get()
        val serverConfigurationService: ServerConfigurationService = get()

        var currentContent by rememberSaveable { mutableStateOf(LoginScreenContent.LOGIN) }

        AnimatedContent(
            targetState = currentContent,
            transitionSpec = {
                // Animation is always the same
                slideInHorizontally { width -> width } with
                        slideOutHorizontally { width -> -width }
            }
        ) { content ->
            when (content) {
                LoginScreenContent.LOGIN -> {
                    LoginUiScreen(
                        modifier = Modifier.fillMaxSize(),
                        onLoggedIn = {
                            // Only display notification settings on the first login for the server
                            scope.launch {
                                val serverUrl = serverConfigurationService.serverUrl.first()
                                if (serverNotificationStorageService.hasDisplayedForServer(serverUrl)) {
                                    onFinishedLoginFlow()
                                } else {
                                    currentContent = LoginScreenContent.NOTIFICATION_SETTINGS
                                }
                            }
                        },
                        onRequestOpenSettings = onRequestOpenSettings
                    )
                }

                LoginScreenContent.NOTIFICATION_SETTINGS -> {
                    NotificationSettingsUi(
                        modifier = Modifier.fillMaxSize(),
                        onDone = {
                            scope.launch {
                                serverNotificationStorageService.setHasDisplayed(
                                    serverConfigurationService.serverUrl.first()
                                )
                                onFinishedLoginFlow()
                            }
                        }
                    )
                }
            }
        }
    }
}

enum class LoginScreenContent {
    LOGIN,
    NOTIFICATION_SETTINGS
}

/**
 * Manages UI directly responsible for logging the user in.
 */
@Composable
private fun LoginUiScreen(
    modifier: Modifier,
    onLoggedIn: () -> Unit,
    onRequestOpenSettings: () -> Unit
) {
    val nestedNavController = rememberNavController()
    val serverConfigurationService: ServerConfigurationService = get()

    val hasSelectedInstance = serverConfigurationService
        .hasUserSelectedInstance
        .collectAsState(initial = null)
        .value
        ?: return // Display nothing to avoid switching between destinations

    val isCustomInstanceSelectionDestination =
        nestedNavController.currentDestination?.route == NESTED_CUSTOM_INSTANCE_SELECTION_DESTINATION

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (isCustomInstanceSelectionDestination) {
                        IconButton(onClick = nestedNavController::navigateUp) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                title = {
                    if (isCustomInstanceSelectionDestination) {
                        Text(text = stringResource(id = R.string.account_select_custom_instance_selection_title))
                    }
                },
                actions = {
                    IconButton(onClick = onRequestOpenSettings) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            navController = nestedNavController,
            startDestination = if (hasSelectedInstance) NESTED_HOME_DESTINATION else NESTED_INSTANCE_SELECTION_DESTINATION
        ) {
            composable(NESTED_HOME_DESTINATION) {
                AccountScreen(
                    modifier = Modifier.fillMaxSize(),
                    onNavigateToLoginScreen = {
                        nestedNavController.navigate(NESTED_LOGIN_DESTINATION)
                    },
                    onNavigateToRegisterScreen = {
                        nestedNavController.navigate(NESTED_REGISTER_DESTINATION)
                    },
                    onNavigateToInstanceSelection = {
                        nestedNavController.navigate(NESTED_INSTANCE_SELECTION_DESTINATION) {
                            popUpTo(NESTED_HOME_DESTINATION) {
                                inclusive = true
                            }
                        }
                    },
                    onLoggedIn = onLoggedIn
                )
            }

            composable(NESTED_CUSTOM_INSTANCE_SELECTION_DESTINATION) {
                CustomInstanceSelectionScreen(
                    modifier = Modifier.fillMaxSize()
                ) {
                    nestedNavController.navigate(NESTED_HOME_DESTINATION) {
                        popUpTo(NESTED_INSTANCE_SELECTION_DESTINATION) {
                            inclusive = true
                        }
                    }
                }
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

            composable(NESTED_INSTANCE_SELECTION_DESTINATION) {
                val scope = rememberCoroutineScope()

                InstanceSelectionScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    availableInstances = ArtemisInstances.instances,
                    onSelectArtemisInstance = { serverUrl ->
                        scope.launch {
                            serverConfigurationService.updateServerUrl(serverUrl)
                            nestedNavController.navigate(NESTED_HOME_DESTINATION) {
                                popUpTo(NESTED_INSTANCE_SELECTION_DESTINATION) {
                                    inclusive = true
                                }
                            }
                        }
                    },
                    onRequestOpenCustomInstanceSelection = {
                        nestedNavController.navigate(
                            NESTED_CUSTOM_INSTANCE_SELECTION_DESTINATION
                        )
                    }
                )
            }
        }
    }

}

/**
 * Displays the screen to login and register. Also allows to change the artemis instance.
 */
@Composable
private fun AccountScreen(
    modifier: Modifier,
    viewModel: AccountViewModel = koinViewModel(),
    onNavigateToLoginScreen: () -> Unit,
    onNavigateToRegisterScreen: () -> Unit,
    onNavigateToInstanceSelection: () -> Unit,
    onLoggedIn: () -> Unit
) {
    val serverProfileInfo by viewModel.serverProfileInfo.collectAsState()

    AccountUi(
        modifier = modifier,
        serverProfileInfo = serverProfileInfo,
        retryLoadServerProfileInfo = viewModel::requestReloadServerProfileInfo,
        onNavigateToLoginScreen = onNavigateToLoginScreen,
        onNavigateToRegisterScreen = onNavigateToRegisterScreen,
        onNavigateToInstanceSelection = onNavigateToInstanceSelection,
        onLoggedIn = onLoggedIn
    )
}

@Composable
private fun AccountUi(
    modifier: Modifier,
    serverProfileInfo: DataState<ProfileInfo>,
    retryLoadServerProfileInfo: () -> Unit,
    onNavigateToLoginScreen: () -> Unit,
    onNavigateToRegisterScreen: () -> Unit,
    onNavigateToInstanceSelection: () -> Unit,
    onLoggedIn: () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.05f)
        )

        ArtemisHeader(modifier = Modifier.fillMaxWidth())

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

        ClickableText(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp),
            text = AnnotatedString(stringResource(id = R.string.account_change_artemis_instance_label)),
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.tertiary),
            onClick = { onNavigateToInstanceSelection() }
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
    Crossfade(
        modifier = modifier,
        targetState = serverProfileInfo,
        animationSpec = tween(50)
    ) { currentServerProfileInfo ->
        Box(modifier = Modifier.fillMaxWidth()) {
            BasicDataStateUi(
                modifier = Modifier.fillMaxWidth(),
                dataState = currentServerProfileInfo,
                loadingText = stringResource(id = R.string.account_load_server_profile_loading),
                failureText = stringResource(id = R.string.account_load_server_profile_failure),
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
internal fun ArtemisHeader(modifier: Modifier) {
    Column(modifier = modifier) {
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
    }
}

@Composable
@Preview(name = "LOADING: profile info")
fun AccountUiPreviewLoadingProfileInfo() {
    AccountUi(
        modifier = Modifier.fillMaxSize(),
        serverProfileInfo = DataState.Loading(),
        retryLoadServerProfileInfo = {},
        onNavigateToLoginScreen = {},
        onNavigateToRegisterScreen = {},
        onNavigateToInstanceSelection = {}
    ) {}
}

@Composable
@Preview(name = "FAILED: profile info")
fun AccountUiPreviewFailedLoadingProfileInfo() {
    AccountUi(
        modifier = Modifier.fillMaxSize(),
        serverProfileInfo = DataState.Failure(IOException()),
        retryLoadServerProfileInfo = {},
        onNavigateToLoginScreen = {},
        onNavigateToRegisterScreen = {},
        onNavigateToInstanceSelection = {}
    ) {}
}

@Composable
@Preview(name = "Registration enabled")
fun AccountUiPreviewWithRegister() {
    AccountUi(
        modifier = Modifier.fillMaxSize(),
        serverProfileInfo = DataState.Success(
            ProfileInfo(
                registrationEnabled = true
            )
        ),
        retryLoadServerProfileInfo = {},
        onNavigateToLoginScreen = {},
        onNavigateToRegisterScreen = {},
        onNavigateToInstanceSelection = {}
    ) {}
}

@Composable
@Preview(name = "Registration disabled")
fun AccountUiPreviewWithoutRegister() {
    AccountUi(
        modifier = Modifier.fillMaxSize(),
        serverProfileInfo = DataState.Success(
            ProfileInfo(
                registrationEnabled = false
            )
        ),
        retryLoadServerProfileInfo = {},
        onNavigateToLoginScreen = {},
        onNavigateToRegisterScreen = {},
        onNavigateToInstanceSelection = {}
    ) {}
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