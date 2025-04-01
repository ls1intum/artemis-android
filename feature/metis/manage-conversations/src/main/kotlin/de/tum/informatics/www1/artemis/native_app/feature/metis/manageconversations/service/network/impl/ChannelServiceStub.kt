package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.network.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.network.ChannelService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

object ChannelServiceStub : ChannelService {

    override val onReloadRequired: Flow<Unit> = flowOf(Unit)

    override suspend fun getChannels(
    ): NetworkResponse<List<ChannelChat>> {
        return NetworkResponse.Response(listOf(ChannelChat(id = 1, name = "Chat")))
    }

    override suspend fun getUnresolvedChannels(
        channelIds: List<Long>,
    ): NetworkResponse<List<Conversation>> {
        return NetworkResponse.Response(listOf(ChannelChat(id = 1, name = "Chat")))
    }

    override suspend fun getExerciseChannel(
        exerciseId: Long,
    ): NetworkResponse<ChannelChat> {
        return NetworkResponse.Response(ChannelChat(id = 1, name = "Exercise Chat"))
    }

    override suspend fun getLectureChannel(
        lectureId: Long,
    ): NetworkResponse<ChannelChat> {
        return NetworkResponse.Response(ChannelChat(id = 1, name = "Lecture Chat"))
    }

    override suspend fun registerInChannel(
        conversationId: Long,
    ): NetworkResponse<Boolean> {
        return NetworkResponse.Response(true)
    }
}