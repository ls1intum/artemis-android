package de.tum.informatics.www1.artemis.native_app.feature.settings.ui.passkeys

import android.util.Log
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.performAutoReloadingNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.passkey.CredentialManagerWrapper
import de.tum.informatics.www1.artemis.native_app.core.data.service.passkey.PasskeySettingsService
import de.tum.informatics.www1.artemis.native_app.core.data.service.passkey.WebauthnApiService
import de.tum.informatics.www1.artemis.native_app.core.data.service.passkey.dto.PasskeyDTO
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow

private const val TAG = "PasskeysUseCase"

class PasskeysUseCase(
    private val networkStatusProvider: NetworkStatusProvider,
    private val passkeySettingsService: PasskeySettingsService,
    private val webauthnApiService: WebauthnApiService,
    private val credentialManagerWrapper: CredentialManagerWrapper,
    private val requestReload: Flow<Unit>,
    private val coroutineScope: CoroutineScope,
) {

    val passkeys: StateFlow<DataState<List<PasskeyDTO>>> = passkeySettingsService.performAutoReloadingNetworkCall(
        networkStatusProvider = networkStatusProvider,
        manualReloadFlow = requestReload
    ) {
        getPasskeys()
    }
        .stateIn(coroutineScope, SharingStarted.Lazily)


    fun createPasskey(): Deferred<Boolean> {
        return coroutineScope.async {
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
                (result as CredentialManagerWrapper.PasskeyCreationResult.Success).registrationResponseJson

            Log.d(TAG, "Passkey creation result: $publicKeyCredentialResponseJson")
            Log.d(TAG, "Register passkey with server...")

            val registrationServerResponse = webauthnApiService.registerPasskey(
                publicKeyCredentialResponseJson,
            )

            Log.d(TAG, "Server response: $registrationServerResponse")

            registrationServerResponse.bind { it.successful }.or(false)
        }
    }
}