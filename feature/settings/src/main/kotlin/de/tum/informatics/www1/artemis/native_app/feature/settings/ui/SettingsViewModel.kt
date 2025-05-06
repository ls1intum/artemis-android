package de.tum.informatics.www1.artemis.native_app.feature.settings.ui

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.app_version.AppVersionProvider
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.performAutoReloadingNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.data.service.passkey.CredentialManagerWrapper
import de.tum.informatics.www1.artemis.native_app.core.data.service.passkey.PasskeySettingsService
import de.tum.informatics.www1.artemis.native_app.core.data.service.passkey.WebauthnApiService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.isLoggedIn
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import de.tum.informatics.www1.artemis.native_app.core.ui.ReloadableViewModel
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationJobService
import de.tum.informatics.www1.artemis.native_app.feature.push.unsubscribeFromNotifications
import de.tum.informatics.www1.artemis.native_app.feature.settings.service.ChangeProfilePictureService
import de.tum.informatics.www1.artemis.native_app.feature.settings.ui.passkeys.PasskeysUseCase
import de.tum.informatics.www1.artemis.native_app.feature.settings.ui.util.ProfilePictureBitmapUtil
import de.tum.informatics.www1.artemis.native_app.feature.settings.ui.util.ProfilePictureUploadResult
import io.ktor.http.ContentType
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class SettingsViewModel(
    private val accountService: AccountService,
    accountDataService: AccountDataService,
    networkStatusProvider: NetworkStatusProvider,
    private val pushNotificationJobService: PushNotificationJobService,
    private val pushNotificationConfigurationService: PushNotificationConfigurationService,
    private val changeProfilePictureService: ChangeProfilePictureService,
    passkeySettingsService: PasskeySettingsService,
    private val webauthnApiService: WebauthnApiService,
    private val credentialManagerWrapper: CredentialManagerWrapper,
    appVersionProvider: AppVersionProvider,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ReloadableViewModel() {
    val appVersion = appVersionProvider.appVersion

    val isLoggedIn: StateFlow<Boolean> = accountService.authenticationData.map { it.isLoggedIn }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)

    private val _account = MutableSharedFlow<DataState<Account>>(extraBufferCapacity = 1)
    val account: StateFlow<DataState<Account>> = _account
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    val passkeysUseCase = PasskeysUseCase(
        networkStatusProvider = networkStatusProvider,
        passkeySettingsService = passkeySettingsService,
        webauthnApiService = webauthnApiService,
        credentialManagerWrapper = credentialManagerWrapper,
        coroutineContext = coroutineContext
    )

    init {
        viewModelScope.launch(coroutineContext) {
            accountDataService.performAutoReloadingNetworkCall(
                networkStatusProvider = networkStatusProvider,
                manualReloadFlow = requestReload
            ) {
                getAccountData()
            }.collectLatest {
                _account.emit(it)
            }
        }
    }

    fun onRequestLogout() {
        viewModelScope.launch(coroutineContext) {
            // the user manually logs out. Therefore we need to tell the server asap.
            unsubscribeFromNotifications(
                pushNotificationConfigurationService,
                pushNotificationJobService
            )

            accountService.logout()
        }
    }

    override fun onRequestReload() {
        viewModelScope.launch(coroutineContext) {
            requestReload.emit(Unit)
        }
    }

    fun onDeleteProfilePicture() {
        viewModelScope.launch(coroutineContext) {
            changeProfilePictureService.delete()
            onRequestReload()
        }
    }

    internal fun onUploadProfilePicture(bitmap: ImageBitmap): Deferred<ProfilePictureUploadResult> {
        return viewModelScope.async(coroutineContext) {
            val resized =
                ProfilePictureBitmapUtil.ensureSizeConstraints(bitmap.asAndroidBitmap())
            val byteArray = ProfilePictureBitmapUtil.toJpegCompressedByteArray(resized)
                ?: return@async ProfilePictureUploadResult.ImageCouldNotBeCompressed

            val response = changeProfilePictureService.upload(
                imageContentType = ContentType.Image.JPEG,
                fileBytes = byteArray,
            )

            if (response is NetworkResponse.Failure) return@async ProfilePictureUploadResult.UploadFailed

            val updatedAccount = (response as NetworkResponse.Response).data
            _account.emit(DataState.Success(updatedAccount))

            ProfilePictureUploadResult.Success
        }
    }
}