package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.saved_posts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.SavedPostService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ISavedPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class SavedPostsViewModel(
    private val courseId: Long,
    val savedPostStatus: SavedPostStatus,
    private val savedPostService: SavedPostService,
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    private val networkStatusProvider: NetworkStatusProvider,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ViewModel() {

    private val onRequestReload = MutableSharedFlow<Unit>(extraBufferCapacity = 1)


    val savedPosts: StateFlow<DataState<List<ISavedPost>>> = flatMapLatest(
        serverConfigurationService.serverUrl,
        accountService.authToken,
        onRequestReload.onStart { emit(Unit) }
    ) { serverUrl, authToken, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            savedPostService.getSavedPosts(
                status = savedPostStatus,
                courseId = courseId,
                authToken = authToken,
                serverUrl = serverUrl
            ).bind { it as List<ISavedPost> }       // Required by the compiler
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)



    fun requestReload() {
        onRequestReload.tryEmit(Unit)
    }
}