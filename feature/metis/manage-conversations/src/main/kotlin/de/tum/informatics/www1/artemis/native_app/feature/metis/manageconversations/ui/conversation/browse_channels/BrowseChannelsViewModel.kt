package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.browse_channels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.onSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.service.performAutoReloadingNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.network.ChannelService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal class BrowseChannelsViewModel(
    private val courseId: Long,
    private val channelService: ChannelService,
    networkStatusProvider: NetworkStatusProvider,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ViewModel() {

    private val requestRefresh = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val allChannels: StateFlow<DataState<List<ChannelChat>>> = channelService
        .performAutoReloadingNetworkCall(
            networkStatusProvider = networkStatusProvider,
            manualReloadFlow = requestRefresh
        ) {
            getChannels(courseId)
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    private val channels = combine(
        allChannels,
        query
    ) {channels, query ->
        channels.bind { channels ->
            channels.filter { it.name.contains(query, ignoreCase = true) }
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

    fun updateQuery(query: String) {
        _query.value = query
    }

    fun requestReload() {
        requestRefresh.tryEmit(Unit)
    }
}