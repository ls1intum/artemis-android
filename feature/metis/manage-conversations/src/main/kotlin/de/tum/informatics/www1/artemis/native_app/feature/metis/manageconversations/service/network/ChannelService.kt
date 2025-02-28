package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.network

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.ArtemisContextBasedService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation

interface ChannelService : ArtemisContextBasedService {

    suspend fun getChannels(courseId: Long): NetworkResponse<List<ChannelChat>>

    suspend fun getExerciseChannel(
        exerciseId: Long,
        courseId: Long
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
    ): NetworkResponse<List<Conversation>>

    suspend fun registerInChannel(
        courseId: Long,
        conversationId: Long
    ): NetworkResponse<Boolean>
}
