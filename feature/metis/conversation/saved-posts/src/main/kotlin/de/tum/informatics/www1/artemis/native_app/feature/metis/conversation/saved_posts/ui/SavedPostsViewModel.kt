package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.saved_posts.ui

import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.keepSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.onSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.saved_posts.service.SavedPostService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.asMetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ISavedPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostStatus
import de.tum.informatics.www1.artemis.native_app.core.ui.ReloadableViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class SavedPostsViewModel(
    val courseId: Long,
    val savedPostStatus: SavedPostStatus,
    private val savedPostService: SavedPostService,
    serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val networkStatusProvider: NetworkStatusProvider,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ReloadableViewModel() {

    val serverUrl: StateFlow<String> = serverConfigurationService.serverUrl
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, "")

    val savedPosts: StateFlow<DataState<List<ISavedPost>>> = flatMapLatest(
        serverUrl,
        accountService.authToken,
        requestReload
    ) { serverUrl, authToken, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            savedPostService.getSavedPosts(
                status = savedPostStatus,
                courseId = courseId,
                authToken = authToken,
                serverUrl = serverUrl
            ).bind { it as List<ISavedPost> }       // Required by the compiler
                // TODO: this is currently required of a bug allowing duplicate items in the list
                //  https://github.com/ls1intum/artemis-android/issues/307
                .bind {
                    it.distinctBy { savedPost -> savedPost.key }
                }
        }
    }
        .keepSuccess()
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    fun changeSavedPostStatus(
        savedPost: ISavedPost,
        newStatus: SavedPostStatus
    ): Deferred<MetisModificationFailure?> {
        return viewModelScope.async(coroutineContext) {
            savedPostService.changeSavedPostStatus(
                post = savedPost,
                status = newStatus,
                authToken = accountService.authToken.first(),
                serverUrl = serverUrl.first()
            )
                .onSuccess { onRequestReload() }
                .asMetisModificationFailure(MetisModificationFailure.CHANGE_SAVED_POST_STATUS)
        }
    }

    fun removeFromSavedPosts(savedPost: ISavedPost): Deferred<MetisModificationFailure?> {
        return viewModelScope.async(coroutineContext) {
            savedPostService.deleteSavedPost(
                post = savedPost,
                authToken = accountService.authToken.first(),
                serverUrl = serverUrl.first()
            )
                .onSuccess { onRequestReload() }
                .asMetisModificationFailure(MetisModificationFailure.CHANGE_SAVED_POST_STATUS)
        }
    }
}