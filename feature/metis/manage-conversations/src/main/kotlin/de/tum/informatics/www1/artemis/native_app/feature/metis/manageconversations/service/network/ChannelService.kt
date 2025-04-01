package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.network

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.CourseBasedService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation

interface ChannelService : CourseBasedService {

    suspend fun getChannels(): NetworkResponse<List<ChannelChat>>

    suspend fun getExerciseChannel(
        exerciseId: Long,
    ): NetworkResponse<ChannelChat>

    suspend fun getLectureChannel(
        lectureId: Long,
    ): NetworkResponse<ChannelChat>

    suspend fun getUnresolvedChannels(
        channelIds: List<Long>,
    ): NetworkResponse<List<Conversation>>

    suspend fun registerInChannel(
        conversationId: Long
    ): NetworkResponse<Boolean>
}
