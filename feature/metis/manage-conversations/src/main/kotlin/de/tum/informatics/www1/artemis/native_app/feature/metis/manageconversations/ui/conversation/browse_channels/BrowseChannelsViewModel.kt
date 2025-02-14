package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.browse_channels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.onSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.network.ChannelService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal class BrowseChannelsViewModel(
    private val courseId: Long,
    private val channelService: ChannelService,
    private val networkStatusProvider: NetworkStatusProvider,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ViewModel() {

    private val requestRefresh = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val channels: StateFlow<DataState<List<ChannelChat>>> = flatMapLatest(
        channelService.onReloadRequired,
        requestRefresh.onStart { emit(Unit) }
    ) { _, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            channelService
                .getChannels(courseId)
                .bind { channels -> channels }
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)


    /**
     * Returns the id of the channel on registration success or null if any error occurred
     */
    fun registerInChannel(channelChat: ChannelChat): Deferred<Long?> {
        return viewModelScope.async(coroutineContext) {
            val result = channelService.registerInChannel(
                courseId = courseId,
                conversationId = channelChat.id
            )

            result.onSuccess { successful ->
                if (successful) {
                    requestReload()
                }
            }

            result.bind { if (it) channelChat.id else null }
                .orNull()
        }
    }


    fun requestReload() {
        requestRefresh.tryEmit(Unit)
    }
}