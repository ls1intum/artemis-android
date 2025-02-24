package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.network

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation

interface ChannelService {

    suspend fun getChannels(
        courseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<List<ChannelChat>>

    suspend fun getExerciseChannel(
        exerciseId: Long,
        courseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<ChannelChat>

    suspend fun getLectureChannel(
        lectureId: Long,
        courseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<ChannelChat>

    suspend fun getUnresolvedChannels(
        courseId: Long,
        channelIds: List<Long>,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<List<Conversation>>

    suspend fun registerInChannel(
        courseId: Long,
        conversationId: Long,
        username: String,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Boolean>
}
