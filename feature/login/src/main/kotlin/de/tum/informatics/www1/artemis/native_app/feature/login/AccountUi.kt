package de.tum.informatics.www1.artemis.native_app.feature.login

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
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
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.animatedComposable
import de.tum.informatics.www1.artemis.native_app.feature.login.custom_instance_selection.CustomInstanceSelectionScreen
import de.tum.informatics.www1.artemis.native_app.feature.login.instance_selection.InstanceSelectionBottomSheet
import de.tum.informatics.www1.artemis.native_app.feature.login.login.LoginScreen
import de.tum.informatics.www1.artemis.native_app.feature.login.login.LoginUi
import de.tum.informatics.www1.artemis.native_app.feature.login.register.RegisterUi
import de.tum.informatics.www1.artemis.native_app.feature.login.saml2_login.Saml2LoginScreen
import de.tum.informatics.www1.artemis.native_app.feature.login.saml2_login.Saml2LoginViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import java.io.IOException


@Serializable
private sealed interface NestedDestination {
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


/**
 * Manages UI directly responsible for logging the user in.
 */
@Composable
internal fun LoginUiScreen(
    modifier: Modifier,
    onLoggedIn: () -> Unit,
    onRequestOpenSettings: () -> Unit,
    onNavigatedToInstanceSelection: () -> Unit
) {
    val nestedNavController = rememberNavController()
    val serverConfigurationService: ServerConfigurationService = koinInject()

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
        var showInstanceSelectionBottomSheet by remember { mutableStateOf(false) }

        NavHost(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                .padding(horizontal = Spacings.ScreenHorizontalSpacing),
            navController = nestedNavController,
            startDestination = NestedDestination.Home
        ) {
            animatedComposable<NestedDestination.Home> {
                AccountScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    canSwitchInstance = !BuildConfig.hasInstanceRestriction,
                    onNavigateToLoginScreen = {
                        nestedNavController.navigate(NestedDestination.Login)
                    },
                    onNavigateToRegisterScreen = {
                        nestedNavController.navigate(NestedDestination.Register)
                    },
                    onNavigateToInstanceSelection = {
                        onNavigatedToInstanceSelection()
                        showInstanceSelectionBottomSheet = true
                    },
                    onLoggedIn = onLoggedIn,
                    onClickSaml2Login = onClickSaml2Login
                )
            }

            animatedComposable<NestedDestination.CustomInstanceSelection> {
                CustomInstanceSelectionScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                ) {
                    nestedNavController.navigate(NestedDestination.Home)
                }
            }

            animatedComposable<NestedDestination.Login> {
                LoginScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding(),
                    viewModel = koinViewModel(),
                    onLoggedIn = onLoggedIn,
                    onClickSaml2Login = onClickSaml2Login
                )
            }

            animatedComposable<NestedDestination.Saml2Login> { backStack ->
                val rememberMe = backStack.toRoute<NestedDestination.Saml2Login>().rememberMe

                val saml2LoginViewModel: Saml2LoginViewModel =
                    koinViewModel { parametersOf(rememberMe) }

                Saml2LoginScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding(),
                    viewModel = saml2LoginViewModel,
                    onLoggedIn = onLoggedIn
                )
            }

            animatedComposable<NestedDestination.Register> {
                RegisterUi(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .imePadding(),
                    viewModel = koinViewModel(),
                    onRegistered = {
                        nestedNavController.popBackStack()
                        nestedNavController.navigate(NestedDestination.Login)
                    }
                )
            }
        }

        if (showInstanceSelectionBottomSheet) {
            InstanceSelectionBottomSheet(
                onDismiss = {
                    showInstanceSelectionBottomSheet = false
                },
                onSelectArtemisInstance = {
                    serverConfigurationService.updateServerUrl(it)
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
    val selectedInstance by viewModel.selectedArtemisInstance.collectAsState()

    AccountUi(
        modifier = modifier,
        serverProfileInfo = serverProfileInfo,
        selectedInstance = selectedInstance,
        canSwitchInstance = canSwitchInstance,
        retryLoadServerProfileInfo = viewModel::onRequestReload,
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
    selectedInstance: ArtemisInstances.ArtemisInstance,
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
            .imePadding()
            .systemBarsPadding()
    ) {
        Spacer(modifier = Modifier.fillMaxHeight(0.05f))

        ArtemisHeader(
            modifier = Modifier.fillMaxWidth(),
            selectedInstance = selectedInstance
        )

        Spacer(modifier = Modifier.fillMaxHeight(0.05f))

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = onNavigateToInstanceSelection
                ) {
                    Text(
                        text = stringResource(id = R.string.account_change_artemis_instance_label),
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.linkTextColor)
                    )
                }
            }
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
                            accountName = "TUM",
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

        HorizontalDivider(
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
internal fun ArtemisHeader(
    modifier: Modifier,
    selectedInstance: ArtemisInstances.ArtemisInstance,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(id = R.string.account_screen_title),
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            modifier = modifier.padding(horizontal = 16.dp)
        )

        Text(
            text = stringResource(id = R.string.account_screen_subtitle),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal),
            textAlign = TextAlign.Center,
            modifier = modifier.padding(horizontal = 32.dp)
        )

        if (BuildConfig.DEBUG || selectedInstance.type == ArtemisInstances.ArtemisInstance.Type.CUSTOM) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = selectedInstance.host,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
@Preview(name = "LOADING: profile info")
fun AccountUiPreviewLoadingProfileInfo() {
    AccountUi(
        modifier = Modifier.fillMaxSize(),
        canSwitchInstance = true,
        serverProfileInfo = DataState.Loading(),
        selectedInstance = ArtemisInstances.TumArtemis,
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
        selectedInstance = ArtemisInstances.TumArtemis,
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
        selectedInstance = ArtemisInstances.TumArtemis,
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
        selectedInstance = ArtemisInstances.TumArtemis,
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