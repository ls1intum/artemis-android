package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.network.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.network.ChannelService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat

object ChannelServiceStub: ChannelService {
    override suspend fun getChannels(
        courseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<List<ChannelChat>> {
        return NetworkResponse.Response(listOf(ChannelChat(id = 1, name = "Chat")))
    }

    override suspend fun getExerciseChannel(
        exerciseId: Long,
        courseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<ChannelChat> {
        return NetworkResponse.Response(ChannelChat(id = 1, name = "Exercise Chat"))
    }

    override suspend fun getLectureChannel(
        lectureId: Long,
        courseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<ChannelChat> {
        return NetworkResponse.Response(ChannelChat(id = 1, name = "Lecture Chat"))
    }

    override suspend fun registerInChannel(
        courseId: Long,
        conversationId: Long,
        username: String,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Boolean> {
        return NetworkResponse.Response(true)
    }
}
