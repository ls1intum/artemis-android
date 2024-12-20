package de.tum.informatics.www1.artemis.native_app.feature.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.defaults.ArtemisInstances
import de.tum.informatics.www1.artemis.native_app.core.model.server_config.ProfileInfo
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.linkTextColor
import de.tum.informatics.www1.artemis.native_app.feature.login.custom_instance_selection.CustomInstanceSelectionScreen
import de.tum.informatics.www1.artemis.native_app.feature.login.instance_selection.InstanceSelectionScreen
import de.tum.informatics.www1.artemis.native_app.feature.login.login.LoginScreen
import de.tum.informatics.www1.artemis.native_app.feature.login.login.LoginUi
import de.tum.informatics.www1.artemis.native_app.feature.login.register.RegisterUi
import de.tum.informatics.www1.artemis.native_app.feature.login.saml2_login.Saml2LoginScreen
import de.tum.informatics.www1.artemis.native_app.feature.login.saml2_login.Saml2LoginViewModel
import de.tum.informatics.www1.artemis.native_app.feature.login.service.ServerNotificationStorageService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import java.io.IOException

private const val ARG_REMEMBER_ME = "rememberMe"
private const val NESTED_SAML2_LOGIN_ROUTE = "saml2_login"

@Serializable
private sealed interface NestedDestination {
    @Serializable
    data object InstanceSelection : NestedDestination
    @Serializable
    data object CustomInstanceSelection : NestedDestination
    @Serializable
    data object Home : NestedDestination
    @Serializable
    data object Login : NestedDestination
    @Serializable
    data object Register : NestedDestination
    @Serializable
    data class Saml2Login(val rememberMe: Boolean) : NestedDestination
}

@Serializable
data class LoginScreen(val nextDestination: String?)

/**
 * @param nextDestination the deep link to a destination that should be opened after a successful login
 */
fun NavController.navigateToLogin(
    nextDestination: String? = null,
    builder: NavOptionsBuilder.() -> Unit
) {
    val screen = if (nextDestination != null) {
        LoginScreen(nextDestination)
    } else {
        LoginScreen(null)
    }

    navigate(screen, builder)
}

/**
 * Switch between actual login and notification configuration.
 */
fun NavGraphBuilder.loginScreen(
    onFinishedLoginFlow: (deepLink: String?) -> Unit,
    onRequestOpenSettings: () -> Unit
) {
    composable<LoginScreen> {
        val screen = it.toRoute<LoginScreen>()
        val nextDestinationValue = screen.nextDestination

        var nextDestination by remember(nextDestinationValue) {
            mutableStateOf(if (nextDestinationValue == null || nextDestinationValue == "null") null else nextDestinationValue)
        }

        val scope = rememberCoroutineScope()
        val serverNotificationStorageService: ServerNotificationStorageService = koinInject()
        val serverConfigurationService: ServerConfigurationService = koinInject()

        var currentContent by rememberSaveable { mutableStateOf(LoginScreenContent.LOGIN) }

        val onFinishedLoginFlowImpl = {
            onFinishedLoginFlow(nextDestination)
        }

        AnimatedContent(
            targetState = currentContent,
            transitionSpec = {
                // Animation is always the same
                slideInHorizontally { width -> width } togetherWith
                        slideOutHorizontally { width -> -width }
            },
            label = "Login <-> Notification configuration"
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
                                    onFinishedLoginFlowImpl()
                                } else {
                                    currentContent = LoginScreenContent.NOTIFICATION_SETTINGS
                                }
                            }
                        },
                        onRequestOpenSettings = onRequestOpenSettings,
                        onNavigatedToInstanceSelection = {
                            nextDestination = null
                        }
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
                                onFinishedLoginFlowImpl()
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
    onRequestOpenSettings: () -> Unit,
    onNavigatedToInstanceSelection: () -> Unit
) {
    val nestedNavController = rememberNavController()
    val serverConfigurationService: ServerConfigurationService = koinInject()

    val hasSelectedInstance = serverConfigurationService
        .hasUserSelectedInstance
        .collectAsState(initial = null)
        .value
        ?: return // Display nothing to avoid switching between destinations

    // Force recomposition
    val currentBackStack by nestedNavController.currentBackStackEntryAsState()
    nestedNavController.currentBackStackEntryAsState().value
    val supportsBackNavigation = nestedNavController.previousBackStackEntry != null

    val selectedDestination: NestedDestination? = currentBackStack?.toRoute()

    val onClickSaml2Login: (rememberMe: Boolean) -> Unit = { rememberMe ->
        nestedNavController.navigate(NestedDestination.Saml2Login(rememberMe))
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (supportsBackNavigation) {
                        IconButton(onClick = nestedNavController::navigateUp) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                title = {
                    val titleText: Int? = when (selectedDestination) {
                        NestedDestination.CustomInstanceSelection -> R.string.account_select_custom_instance_selection_title
                        NestedDestination.Login -> R.string.login_title
                        NestedDestination.Register -> R.string.register_title
                        else -> null
                    }

                    if (titleText != null) {
                        Text(text = stringResource(id = titleText))
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
                .imePadding()
                .consumeWindowInsets(WindowInsets.systemBars)
                .padding(top = paddingValues.calculateTopPadding()),
            navController = nestedNavController,
            startDestination = if (hasSelectedInstance) NestedDestination.Home else NestedDestination.InstanceSelection
        ) {
            composable<NestedDestination.Home>() {
                AccountScreen(
                    modifier = Modifier.fillMaxSize(),
                    canSwitchInstance = !BuildConfig.hasInstanceRestriction,
                    onNavigateToLoginScreen = {
                        nestedNavController.navigate(NestedDestination.Login)
                    },
                    onNavigateToRegisterScreen = {
                        nestedNavController.navigate(NestedDestination.Register)
                    },
                    onNavigateToInstanceSelection = {
                        onNavigatedToInstanceSelection()

                        nestedNavController.navigate(NestedDestination.InstanceSelection) {
                            popUpTo<NestedDestination.Home> {
                                inclusive = true
                            }
                        }
                    },
                    onLoggedIn = onLoggedIn,
                    onClickSaml2Login = onClickSaml2Login
                )
            }

            composable<NestedDestination.CustomInstanceSelection> {
                CustomInstanceSelectionScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    nestedNavController.navigate(NestedDestination.Home) {
                        popUpTo<NestedDestination.InstanceSelection> {
                            inclusive = true
                        }
                    }
                }
            }

            composable<NestedDestination.Login> {
                LoginScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = koinViewModel(),
                    onLoggedIn = onLoggedIn,
                    onClickSaml2Login = onClickSaml2Login
                )
            }

            composable<NestedDestination.Saml2Login> { backStack ->
                val rememberMe = backStack.arguments?.getBoolean(ARG_REMEMBER_ME)
                checkNotNull(rememberMe)

                val saml2LoginViewModel: Saml2LoginViewModel =
                    koinViewModel { parametersOf(rememberMe) }

                Saml2LoginScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = saml2LoginViewModel,
                    onLoggedIn = onLoggedIn
                )
            }

            composable<NestedDestination.Register> {
                RegisterUi(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Spacings.ScreenHorizontalSpacing),
                    viewModel = koinViewModel(),
                    onRegistered = {
                        nestedNavController.popBackStack()
                        nestedNavController.navigate(NestedDestination.Login)
                    }
                )
            }

            composable<NestedDestination.InstanceSelection> {
                val scope = rememberCoroutineScope()

                InstanceSelectionScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Spacings.ScreenHorizontalSpacing),
                    availableInstances = ArtemisInstances.instances,
                    onSelectArtemisInstance = { serverUrl ->
                        scope.launch {
                            serverConfigurationService.updateServerUrl(serverUrl)
                            nestedNavController.navigate(NestedDestination.Home) {
                                popUpTo<NestedDestination.InstanceSelection> {
                                    inclusive = true
                                }
                            }
                        }
                    },
                    onRequestOpenCustomInstanceSelection = {
                        nestedNavController.navigate(
                            NestedDestination.CustomInstanceSelection
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
    canSwitchInstance: Boolean,
    onNavigateToLoginScreen: () -> Unit,
    onNavigateToRegisterScreen: () -> Unit,
    onNavigateToInstanceSelection: () -> Unit,
    onLoggedIn: () -> Unit,
    onClickSaml2Login: (rememberMe: Boolean) -> Unit
) {
    val serverProfileInfo by viewModel.serverProfileInfo.collectAsState()

    AccountUi(
        modifier = modifier,
        serverProfileInfo = serverProfileInfo,
        canSwitchInstance = canSwitchInstance,
        retryLoadServerProfileInfo = viewModel::requestReloadServerProfileInfo,
        onNavigateToLoginScreen = onNavigateToLoginScreen,
        onNavigateToRegisterScreen = onNavigateToRegisterScreen,
        onNavigateToInstanceSelection = onNavigateToInstanceSelection,
        onLoggedIn = onLoggedIn,
        onClickSaml2Login = onClickSaml2Login
    )
}

@Composable
private fun AccountUi(
    modifier: Modifier,
    serverProfileInfo: DataState<ProfileInfo>,
    canSwitchInstance: Boolean,
    retryLoadServerProfileInfo: () -> Unit,
    onNavigateToLoginScreen: () -> Unit,
    onNavigateToRegisterScreen: () -> Unit,
    onNavigateToInstanceSelection: () -> Unit,
    onLoggedIn: () -> Unit,
    onClickSaml2Login: (rememberMe: Boolean) -> Unit
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
            onNavigateToRegisterScreen = onNavigateToRegisterScreen,
            onClickSaml2Login = onClickSaml2Login
        )

        if (canSwitchInstance) {
            ClickableText(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(
                        bottom = WindowInsets.systemBars
                            .asPaddingValues()
                            .calculateBottomPadding()
                    ),
                text = AnnotatedString(stringResource(id = R.string.account_change_artemis_instance_label)),
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.linkTextColor),
                onClick = { onNavigateToInstanceSelection() }
            )
        }
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
    onLoggedIn: () -> Unit,
    onClickSaml2Login: (rememberMe: Boolean) -> Unit
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
                            accountName = "TUm",
                            needsToAcceptTerms = true,
                            hasUserAcceptedTerms = true,
                            saml2Config = null,
                            isPasswordLoginDisabled = false,
                            updateUserAcceptedTerms = {},
                            passwordBasedLoginContent = {},
                            saml2BasedLoginContent = { _, _ -> }
                        )
                    } else {
                        LoginUi(
                            modifier = loginUiModifier,
                            viewModel = koinViewModel(),
                            onLoggedIn = onLoggedIn,
                            onClickSaml2Login = onClickSaml2Login
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
        canSwitchInstance = true,
        serverProfileInfo = DataState.Loading(),
        retryLoadServerProfileInfo = {},
        onNavigateToLoginScreen = {},
        onNavigateToRegisterScreen = {},
        onNavigateToInstanceSelection = {},
        onLoggedIn = {},
        onClickSaml2Login = {}
    )
}

@Composable
@Preview(name = "FAILED: profile info")
fun AccountUiPreviewFailedLoadingProfileInfo() {
    AccountUi(
        modifier = Modifier.fillMaxSize(),
        canSwitchInstance = true,
        serverProfileInfo = DataState.Failure(IOException()),
        retryLoadServerProfileInfo = {},
        onNavigateToLoginScreen = {},
        onNavigateToRegisterScreen = {},
        onNavigateToInstanceSelection = {},
        onLoggedIn = {},
        onClickSaml2Login = {}
    )
}

@Composable
@Preview(name = "Registration enabled")
fun AccountUiPreviewWithRegister() {
    AccountUi(
        modifier = Modifier.fillMaxSize(),
        canSwitchInstance = true,
        serverProfileInfo = DataState.Success(
            ProfileInfo(
                registrationEnabled = true
            )
        ),
        retryLoadServerProfileInfo = {},
        onNavigateToLoginScreen = {},
        onNavigateToRegisterScreen = {},
        onNavigateToInstanceSelection = {},
        onLoggedIn = {},
        onClickSaml2Login = {}
    )
}

@Composable
@Preview(name = "Registration disabled")
fun AccountUiPreviewWithoutRegister() {
    AccountUi(
        modifier = Modifier.fillMaxSize(),
        canSwitchInstance = true,
        serverProfileInfo = DataState.Success(
            ProfileInfo(
                registrationEnabled = false
            )
        ),
        retryLoadServerProfileInfo = {},
        onNavigateToLoginScreen = {},
        onNavigateToRegisterScreen = {},
        onNavigateToInstanceSelection = {},
        onLoggedIn = {},
        onClickSaml2Login = {}
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