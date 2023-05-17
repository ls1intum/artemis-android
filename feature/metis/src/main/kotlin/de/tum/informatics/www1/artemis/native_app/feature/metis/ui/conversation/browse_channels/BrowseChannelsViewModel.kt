package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.browse_channels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.CourseService
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.account.isAtLeastTutorInCourse
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.ChannelService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.ConversationService
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

internal class BrowseChannelsViewModel(
    private val courseId: Long,
    private val accountService: AccountService,
    private val serverConfigurationService: ServerConfigurationService,
    private val channelService: ChannelService,
    private val networkStatusProvider: NetworkStatusProvider,
    serverDataService: ServerDataService,
    courseService: CourseService
) : ViewModel() {

    private val requestRefresh = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val channels: StateFlow<DataState<List<ChannelChat>>> = flatMapLatest(
        serverConfigurationService.serverUrl,
        accountService.authToken,
        requestRefresh.onStart { emit(Unit) }
    ) { serverUrl, authToken, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            channelService
                .getChannels(courseId, serverUrl, authToken)
                .bind { channels -> channels.filter { !it.isMember } }
        }
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly)

    val canCreateChannel: StateFlow<Boolean> = flatMapLatest(
        serverConfigurationService.serverUrl,
        accountService.authToken,
        requestRefresh.onStart { emit(Unit) }
    ) { serverUrl, authToken, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            courseService.getCourse(courseId, serverUrl, authToken)
                .then { courseWithScore ->
                    serverDataService
                        .getAccountData(serverUrl, authToken)
                        .bind { it.isAtLeastTutorInCourse(courseWithScore.course) }
                }
        }.map { it.orElse(false) }

    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * Returns the id of the channel on registration success or null if any error occurred
     */
    fun registerInChannel(channelChat: ChannelChat): Deferred<Long?> {
        return viewModelScope.async {
            val username = when (val authData = accountService.authenticationData.first()) {
                is AccountService.AuthenticationData.LoggedIn -> authData.username
                AccountService.AuthenticationData.NotLoggedIn -> return@async null
            }

            channelService.registerInChannel(
                courseId = courseId,
                conversationId = channelChat.id,
                username = username,
                serverUrl = serverConfigurationService.serverUrl.first(),
                authToken = accountService.authToken.first()
            )
                .bind { if (it) channelChat.id else null }
                .orNull()
        }
    }

    fun requestReload() {
        requestRefresh.tryEmit(Unit)
    }
}