package de.tum.informatics.www1.artemis.native_app.feature.settings.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.app_version.AppVersionProvider
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.data.service.performAutoReloadingNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.isLoggedIn
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationJobService
import de.tum.informatics.www1.artemis.native_app.feature.push.unsubscribeFromNotifications
import de.tum.informatics.www1.artemis.native_app.feature.settings.service.ChangeProfilePictureService
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val ALLOWED_MIME_TYPES = setOf("image/jpeg", "image/png")


class SettingsViewModel(
    private val accountService: AccountService,
    accountDataService: AccountDataService,
    networkStatusProvider: NetworkStatusProvider,
    private val pushNotificationJobService: PushNotificationJobService,
    private val pushNotificationConfigurationService: PushNotificationConfigurationService,
    private val changeProfilePictureService: ChangeProfilePictureService,
    appVersionProvider: AppVersionProvider,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ViewModel() {
    private val onRequestReload = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val appVersion = appVersionProvider.appVersion

    val isLoggedIn: StateFlow<Boolean> = accountService.authenticationData.map { it.isLoggedIn }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)

    private val _account = MutableSharedFlow<DataState<Account>>(extraBufferCapacity = 1)
    val account: StateFlow<DataState<Account>> = _account
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    init {
        viewModelScope.launch(coroutineContext) {
            accountDataService.performAutoReloadingNetworkCall(
                networkStatusProvider = networkStatusProvider,
                manualReloadFlow = onRequestReload
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

    fun requestReload() {
        viewModelScope.launch(coroutineContext) {
            onRequestReload.emit(Unit)
        }
    }

    fun onDeleteProfilePicture() {
        viewModelScope.launch(coroutineContext) {
            changeProfilePictureService.delete()
        }
    }

    fun onUploadProfilePicture(uri: Uri, context: Context) {
        viewModelScope.launch(coroutineContext) {
            try {
                val mimeType = context.contentResolver.getType(uri)
                if (mimeType !in ALLOWED_MIME_TYPES) {
                    Toast.makeText(
                        context,
                        "This file type is not supported for profile pictures",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                val fileBytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val fileSize = inputStream.available()

                        val maxFileSize = 5 * 1024 * 1024
                        if (fileSize > maxFileSize) {
                            throw IllegalArgumentException(
                                "File size exceeds the maximum allowed file size of 5 MB"
//                                getString(
//                                    context,
//                                    R.string.conversation_vm_file_size_exceed
//                                )
                            )
                        }
                        inputStream.readBytes()
                    }
                } ?: throw IllegalArgumentException(
                    "Failed to read file bytes"
//                    getString(
//                        context,
//                        R.string.conversation_vm_file_upload_failed
//                    )
                )

                val response = changeProfilePictureService.upload(
                    imageContentType = if (mimeType == "image/jpeg") {
                        ContentType.Image.JPEG
                    } else {
                        ContentType.Image.PNG
                    },
                    fileBytes = fileBytes,
                )

                when (response) {
                    is NetworkResponse.Response -> {
                        val updatedAccount = response.data
                        _account.emit(DataState.Success(updatedAccount))
                    }

                    else -> {
                        throw IllegalArgumentException(
                            "Failed to upload file"
//                            getString(
//                                context,
//                                R.string.conversation_vm_file_upload_failed
//                            )
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}