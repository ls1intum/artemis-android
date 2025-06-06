package de.tum.informatics.www1.artemis.native_app.feature.login.saml2_login

import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.ReloadableViewModel
import de.tum.informatics.www1.artemis.native_app.core.ui.serverUrlStateFlow
import de.tum.informatics.www1.artemis.native_app.feature.login.service.network.LoginService
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class Saml2LoginViewModel(
    private val rememberMe: Boolean,
    serverConfigurationService: ServerConfigurationService,
    networkStatusProvider: NetworkStatusProvider,
    loginService: LoginService,
    private val accountService: AccountService
) : ReloadableViewModel() {

    val saml2LoginResponse: StateFlow<DataState<Saml2LoginResponse>> =
        flatMapLatest(
            requestReload.onStart { emit(Unit) },
            serverConfigurationService.serverUrl
        ) { _, serverUrl ->
            retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                loginService
                    .loginSaml2(rememberMe, serverUrl)
                    .bind { response ->
                        when {
                            response.status.isSuccess() -> Saml2LoginResponse.Success
                            else -> {
                                val errorHeader = response.headers["X-artemisApp-error"].orEmpty()
                                Saml2LoginResponse.Error(response.status.value, errorHeader)
                            }
                        }
                    }
            }
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly)

    val serverUrl: StateFlow<String> = serverUrlStateFlow(serverConfigurationService)

    fun saveAccessToken(token: String, onDone: () -> Unit) {
        viewModelScope.launch {
            accountService.storeAccessToken(token, rememberMe)
            onDone()
        }
    }

    sealed interface Saml2LoginResponse {
        object Success : Saml2LoginResponse

        data class Error(val statusCode: Int, val errorHeader: String) : Saml2LoginResponse
    }
}