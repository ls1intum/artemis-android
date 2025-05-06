package de.tum.informatics.www1.artemis.native_app.feature.settings.ui.passkeys

import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.performAutoReloadingNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.passkey.CredentialManagerWrapper
import de.tum.informatics.www1.artemis.native_app.core.data.service.passkey.PasskeySettingsService
import de.tum.informatics.www1.artemis.native_app.core.data.service.passkey.WebauthnApiService
import de.tum.informatics.www1.artemis.native_app.core.data.service.passkey.dto.PasskeyDTO
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.ReloadableViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class PasskeysUseCase(
    private val networkStatusProvider: NetworkStatusProvider,
    private val passkeySettingsService: PasskeySettingsService,
    private val webauthnApiService: WebauthnApiService,
    private val credentialManagerWrapper: CredentialManagerWrapper,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ReloadableViewModel() {

    val passkeys: StateFlow<DataState<List<PasskeyDTO>>> = passkeySettingsService.performAutoReloadingNetworkCall(
        networkStatusProvider = networkStatusProvider,
        manualReloadFlow = requestReload
    ) {
        getPasskeys()
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Lazily)


    fun createPasskey(): Deferred<Boolean> {
        return viewModelScope.async(coroutineContext) {
            val options = webauthnApiService.getRegistrationOptions()
            if (options is NetworkResponse.Failure) {
                return@async false
            }

            val requestJson = (options as NetworkResponse.Response).data
            val result = credentialManagerWrapper.createPasskey(requestJson)

            if (result is CredentialManagerWrapper.PasskeyCreationResult.Failure) {
                return@async false
            }

            if (result is CredentialManagerWrapper.PasskeyCreationResult.Canceled) {
                return@async true       // We do not consider this a failure
            }

            val publicKeyCredentialResponseJson =
                (result as CredentialManagerWrapper.PasskeyCreationResult.Success)
            // TODO: continue here + maybe create PasskeyManager that contains this logic

            return@async true

//            val response = webauthnApiService.registerPasskey(
//                ,
//            )
//
//            response.bind { it.successful }.or(false)
        }
    }
}