package de.tum.informatics.www1.artemis.native_app.feature.login.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.onSuccess
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerProfileInfoService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.server_config.ProfileInfo
import de.tum.informatics.www1.artemis.native_app.core.ui.serverUrlStateFlow
import de.tum.informatics.www1.artemis.native_app.feature.login.BaseAccountViewModel
import de.tum.informatics.www1.artemis.native_app.feature.login.service.AndroidCredentialService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.LoginMethod
import de.tum.informatics.www1.artemis.native_app.feature.login.service.LoginOptionsDto
import de.tum.informatics.www1.artemis.native_app.feature.login.service.oidc.OidcAuthService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.network.LoginService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.network.PasskeyLoginService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * View model to handle the login process.
 */
class LoginViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val accountService: AccountService,
    private val loginService: LoginService,
    private val pushNotificationConfigurationService: PushNotificationConfigurationService,
    private val serverConfigurationService: ServerConfigurationService,
    serverProfileInfoService: ServerProfileInfoService,
    networkStatusProvider: NetworkStatusProvider,
    private val passkeyLoginService: PasskeyLoginService,
    private val oidcAuthService: OidcAuthService,
    private val androidCredentialService: AndroidCredentialService,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : BaseAccountViewModel(serverConfigurationService, networkStatusProvider, serverProfileInfoService) {

    companion object {
        private const val USERNAME_KEY = "username"
        private const val PASSWORD_KEY = "password"
        private const val REMEMBER_ME_KEY = "rememberMe"
        private const val USER_ACCEPTED_TERMS_KEY = "rememberMe"
        private const val AUTH_PHASE = "authPhase"
    }

    val username: StateFlow<String> = savedStateHandle.getStateFlow(USERNAME_KEY, "")

    val password: StateFlow<String> = savedStateHandle.getStateFlow(PASSWORD_KEY, "")

    val rememberMe: StateFlow<Boolean> = savedStateHandle.getStateFlow(REMEMBER_ME_KEY, true)

    val authPhase: StateFlow<AuthPhase> = savedStateHandle.getStateFlow(AUTH_PHASE, AuthPhase.USERNAME)

    val hasUserAcceptedTerms: StateFlow<Boolean> =
        savedStateHandle.getStateFlow(USER_ACCEPTED_TERMS_KEY, false)

    private val _loginOptions = MutableStateFlow<LoginOptionsDto?>(null)
    val loginOptions: StateFlow<LoginOptionsDto?> = _loginOptions.asStateFlow()

    private val _singleSSOOption = MutableStateFlow<LoginOptionsDto?>(null)
    val singleSSOOption: StateFlow<LoginOptionsDto?> = _singleSSOOption.asStateFlow()

    private val _oidcLoginJob = MutableStateFlow<Deferred<Boolean>?>(null)
    val oidcLoginJob: StateFlow<Deferred<Boolean>?> = _oidcLoginJob.asStateFlow()

    val continueButtonEnabled: StateFlow<Boolean> = username
        .map { it.isNotBlank() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val loginButtonEnabled: StateFlow<Boolean> =
        combine(
            username,
            password,
            hasUserAcceptedTerms,
            serverProfileInfo
        ) { username, password, userAcceptedTerms, serverProfileInfo ->
            val needsToAcceptTerms = when (serverProfileInfo) {
                is DataState.Success -> serverProfileInfo.data.needsToAcceptTerms
                else -> false
            }
            username.isNotBlank() && password.isNotBlank() && (!needsToAcceptTerms || userAcceptedTerms)
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val serverUrl: StateFlow<String> = serverUrlStateFlow(serverConfigurationService)

    init {
        // reset the authentication phase if instance is changed
        viewModelScope.launch(coroutineContext) {
            serverConfigurationService.serverUrl
                .distinctUntilChanged()
                .drop(1)
                .collect {
                    resetToUsernamePhase()
                }
        }
        // if only one authentication options is enabled on instance -> skip USERNAME authentication phase
        viewModelScope.launch(coroutineContext) {
            serverProfileInfo.collect { dataState ->
                if (dataState is DataState.Success) {
                    val singleSso = determineSingleSSOOption(dataState.data)
                    _singleSSOOption.value = singleSso
                    if (singleSso != null) {
                        _loginOptions.value = singleSso
                        updateAuthPhase(AuthPhase.CREDENTIALS)
                    }
                }
            }
        }
        // listen for OIDC code to be extracted and start code exchange routine
        viewModelScope.launch(coroutineContext) {
            oidcAuthService.authCodeFlow.collect { code ->
                _oidcLoginJob.value = handleOidcCallback(code)
            }
        }
    }

    private fun determineSingleSSOOption(profile: ProfileInfo): LoginOptionsDto? {
        if (!profile.isPasswordLoginDisabled) return null

        val hasSaml2 = profile.saml2 != null || profile.activeProfiles.contains("saml2")
        val hasOidc = profile.oidc != null || profile.activeProfiles.contains("oidc")

        return when {
            hasSaml2 && !hasOidc -> {
                val label = profile.saml2?.buttonLabel ?: profile.saml2?.identityProviderName
                LoginOptionsDto(LoginMethod.SAML2, label)
            }
            hasOidc && !hasSaml2 -> {
                val label = profile.oidc?.buttonLabel ?: profile.oidc?.clientName
                LoginOptionsDto(LoginMethod.OIDC, label)
            }
            else -> null
        }
    }

    fun updateUsername(newUsername: String) {
        savedStateHandle[USERNAME_KEY] = newUsername
    }

    fun updatePassword(newPassword: String) {
        savedStateHandle[PASSWORD_KEY] = newPassword
    }

    fun updateRememberMe(newRememberMe: Boolean) {
        savedStateHandle[REMEMBER_ME_KEY] = newRememberMe
    }

    fun updateAuthPhase(newAuthPhase: AuthPhase){
        savedStateHandle[AUTH_PHASE] = newAuthPhase
    }

    fun updateUserAcceptedTerms(newUserAcceptedTerms: Boolean) {
        savedStateHandle[USER_ACCEPTED_TERMS_KEY] = newUserAcceptedTerms
    }

    fun fetchLoginOptions(): Deferred<Boolean> {
        return viewModelScope.async(coroutineContext) {
            val serverUrlVal = serverUrl.value
            val usernameVal = username.first().trim()
            if (usernameVal.isBlank()) return@async false
            when (val response = loginService.fetchLoginOptions(usernameVal, serverUrlVal)) {
                is NetworkResponse.Response -> {
                    android.util.Log.d("LoginViewModel", "Fetched login options: ${response.data}")
                    _loginOptions.value = response.data
                    updateAuthPhase(AuthPhase.CREDENTIALS)
                    return@async true
                }

                is NetworkResponse.Failure -> {
                    android.util.Log.w(
                        "LoginViewModel",
                        "fetchLoginOptions failed, applying fallback",
                        response.exception
                    )
                    // fallback for instances that do not have /login-options integrated yet (404)
                    val profile = serverProfileInfo.value
                    val isPasswordDisabled =
                        profile is DataState.Success && profile.data.isPasswordLoginDisabled
                    // default fallback is a password option, but if this is disabled -> show saml2 for compatibility
                    val fallbackMethod =
                        if (isPasswordDisabled) LoginMethod.SAML2 else LoginMethod.PASSWORD
                    val idpLabel =
                        (profile as? DataState.Success)?.data?.saml2?.identityProviderName

                    _loginOptions.value = LoginOptionsDto(fallbackMethod, idpLabel)
                    updateAuthPhase(AuthPhase.CREDENTIALS)
                    return@async true
                }
            }
        }
    }

    fun resetToUsernamePhase() {
        updateAuthPhase(AuthPhase.USERNAME)
        _loginOptions.value = null
        updatePassword("")
    }

    fun login(): Deferred<Boolean> {
        return viewModelScope.async(coroutineContext) {
            val serverUrl = serverUrl.value
            val rememberMe = rememberMe.first()

            val hasToRegisterForPushNotifications =
                pushNotificationConfigurationService.getArePushNotificationsEnabledFlow(serverUrl)
                    .first()

            loginService.loginWithCredentials(
                username.first(),
                password.first(),
                rememberMe,
                serverUrl
            )
                .then {
                    if (hasToRegisterForPushNotifications) {
                        val wasSuccess =
                            pushNotificationConfigurationService.updateArePushNotificationEnabled(
                                true,
                                serverUrl,
                                it.idToken
                            )

                        if (wasSuccess) NetworkResponse.Response(it) else NetworkResponse.Failure(
                            RuntimeException("Could not register for push notifications")
                        )
                    } else NetworkResponse.Response(it)
                }
                .onSuccess {
                    accountService.storeAccessToken(it.idToken, rememberMe)
                }
                .bind { true }
                .or(false)
        }
    }

    fun loginWithPasskey(): Deferred<Boolean> {
        return viewModelScope.async(coroutineContext) {
            val serverUrl = serverUrl.value
            val hasToRegisterForPushNotifications =
                pushNotificationConfigurationService.getArePushNotificationsEnabledFlow(serverUrl)
                    .first()


            // Get challenge from server
            val challengeResponse = passkeyLoginService.getAuthenticationOptions()
            if (challengeResponse is NetworkResponse.Failure) {
                return@async false
            }

            // Use passkey to create response
            val authenticationOptionsResponse = (challengeResponse as NetworkResponse.Response).data
            val result = androidCredentialService.signIn(authenticationOptionsResponse)
            if (result is AndroidCredentialService.SignInResult.NoCredential) {
                return@async true       // We do not consider this a failure
            }
            if (result is AndroidCredentialService.SignInResult.Failure) {
                return@async false
            }

            // Send response to server
            val publicKeyCredentialResponseJson = (result as AndroidCredentialService.SignInResult.WithPasskey).responseJson
            passkeyLoginService
                .loginWithPasskey(publicKeyCredentialResponseJson)
                .then {
                    if (hasToRegisterForPushNotifications) {
                        val wasSuccess =
                            pushNotificationConfigurationService.updateArePushNotificationEnabled(
                                true,
                                serverUrl,
                                it.idToken
                            )

                        if (wasSuccess) NetworkResponse.Response(it) else NetworkResponse.Failure(
                            RuntimeException("Could not register for push notifications")
                        )
                    } else NetworkResponse.Response(it)
                }
                .onSuccess {
                    accountService.storeAccessToken(it.idToken, rememberMe = true)
                }
                .bind { true }
                .or(false)

//            serverResponse.bind { it.authenticated }.or(false)
        }
    }
    // Start the oidc authentication flow
    fun loginWithOidc() {
        oidcAuthService.launchOidcFlow(
            serverUrl = serverUrl.value,
            rememberMe = rememberMe.value
        )
    }

    // extract code after successful oidc authentication by IdP
    fun handleOidcCallback(code: String): Deferred<Boolean> {
        return viewModelScope.async(coroutineContext) {
            val serverUrlVal = serverUrl.value
            val rememberMeVal = rememberMe.first()
            val verifier = oidcAuthService.getAndClearCodeVerifier() ?: return@async false

            val hasToRegisterForPushNotifications =
                pushNotificationConfigurationService.getArePushNotificationsEnabledFlow(serverUrlVal)
                    .first()

            loginService.exchangeCodeForJwtToken(code, verifier, serverUrlVal)
                .then {
                    if (hasToRegisterForPushNotifications) {
                        val wasSuccess =
                            pushNotificationConfigurationService.updateArePushNotificationEnabled(
                                true,
                                serverUrlVal,
                                it.idToken
                            )
                        if (wasSuccess) NetworkResponse.Response(it)
                        else NetworkResponse.Failure(RuntimeException("Could not register for push notifications"))
                    } else NetworkResponse.Response(it)
                }
                .onSuccess {
                    accountService.storeAccessToken(it.idToken, rememberMeVal)
                }
                .bind { true }
                .or(false)
        }
    }

    fun clearOidcLoginJob() {
        _oidcLoginJob.value = null
    }
}
