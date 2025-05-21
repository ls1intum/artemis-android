package de.tum.informatics.www1.artemis.native_app.feature.settings.ui.passkeys

import android.util.Log
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.performAutoReloadingNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.login.service.AndroidCredentialService
import de.tum.informatics.www1.artemis.native_app.feature.settings.service.PasskeySettingsService
import de.tum.informatics.www1.artemis.native_app.feature.settings.service.dto.PasskeyDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow

private const val TAG = "PasskeysUseCase"

class PasskeysUseCase(
    networkStatusProvider: NetworkStatusProvider,
    private val passkeySettingsService: PasskeySettingsService,
    private val androidCredentialService: AndroidCredentialService,
    requestReload: Flow<Unit>,
    private val coroutineScope: CoroutineScope,
) {

    val passkeys: StateFlow<DataState<List<PasskeyDTO>>> = passkeySettingsService.performAutoReloadingNetworkCall(
        networkStatusProvider = networkStatusProvider,
        manualReloadFlow = requestReload
    ) {
        getPasskeys()
    }
        .stateIn(coroutineScope, SharingStarted.Lazily)

    sealed class CreationResult {
        data object Success : CreationResult()
        data object Cancelled : CreationResult()
        data class Failure(val error: String) : CreationResult()
    }

    fun createPasskey(): Deferred<CreationResult> {
        return coroutineScope.async {
            val options = passkeySettingsService.getRegistrationOptions()
            if (options is NetworkResponse.Failure) {
                return@async CreationResult.Failure(options.exception.localizedMessage ?: "Unknown error")
            }

            val requestJson = (options as NetworkResponse.Response).data
            val result = androidCredentialService.createPasskey(requestJson)

            if (result is AndroidCredentialService.PasskeyCreationResult.Failure) {
                return@async CreationResult.Failure(result.error.localizedMessage ?: "Unknown error")
            }

            if (result is AndroidCredentialService.PasskeyCreationResult.Cancelled) {
                return@async CreationResult.Cancelled
            }

            val publicKeyCredentialResponseJson =
                (result as AndroidCredentialService.PasskeyCreationResult.Success).registrationResponseJson

            val registrationServerResponse = passkeySettingsService.registerPasskey(
                publicKeyCredentialResponseJson,
            )

            when (registrationServerResponse) {
                is NetworkResponse.Failure -> {
                    Log.e(TAG, "Failed to register passkey: ${registrationServerResponse.exception}")
                    CreationResult.Failure(registrationServerResponse.exception.localizedMessage ?: "Unknown error")
                }
                is NetworkResponse.Response -> {
                    val success = registrationServerResponse.data.success
                    if (!success) {
                        Log.e(TAG, "Failed to register passkey: Registration call result was not successful")
                        return@async CreationResult.Failure("Registration call result was not successful")
                    }

                    Log.d(TAG, "Successfully registered passkey")
                    CreationResult.Success
                }
            }
        }
    }
}