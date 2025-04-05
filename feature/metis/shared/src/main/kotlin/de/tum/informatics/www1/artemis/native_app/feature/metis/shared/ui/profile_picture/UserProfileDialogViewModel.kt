package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.performAutoReloadingNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.CommunicationDeeplinks
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class UserProfileDialogViewModel(
    private val courseId: Long,
    private val userId: Long,
    accountDataService: AccountDataService,
    networkStatusProvider: NetworkStatusProvider,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ViewModel() {

    private val clientId: StateFlow<DataState<Long>> = accountDataService.performAutoReloadingNetworkCall(
        networkStatusProvider = networkStatusProvider,
    ) {
        getAccountData().bind { it.id }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Lazily, DataState.Loading())

    val isSendMessageAvailable: StateFlow<Boolean>  = clientId
        .map { dataState ->
            dataState.bind {
                it != userId
            }.orElse(false)
        }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)

    fun navigateToOneToOneChat(context: Context) {
        val chatLink = CommunicationDeeplinks.ToOneToOneChatByUserId.inAppLink(courseId, userId)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(chatLink))
        context.startActivity(intent)
    }
}