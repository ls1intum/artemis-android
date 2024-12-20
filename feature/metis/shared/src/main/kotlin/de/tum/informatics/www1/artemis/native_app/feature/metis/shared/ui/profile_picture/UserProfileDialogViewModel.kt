package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.MetisViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext

class UserProfileDialogViewModel(
    private val courseId: Long,
    private val userId: Long,
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    private val accountDataService: AccountDataService,
    private val networkStatusProvider: NetworkStatusProvider,
    websocketProvider: WebsocketProvider,
    private val coroutineContext: CoroutineContext
) : MetisViewModel(
    serverConfigurationService,
    accountService,
    accountDataService,
    networkStatusProvider,
    websocketProvider,
    coroutineContext
) {

    val isShownUserTheAppUser: StateFlow<DataState<Boolean>>  = clientId
        .map { dataState ->
            dataState.bind {
                it == userId
            }
        }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, DataState.Loading())

    fun navigateToOneToOneChat(context: Context) {
        // TODO: This does currently not work yet, see: https://github.com/ls1intum/artemis-android/issues/213
        val chatLink = "artemis://courses/$courseId/messages?userId=$userId"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(chatLink))
        context.startActivity(intent)
    }
}