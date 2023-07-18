package de.tum.informatics.www1.artemis.native_app.feature.metis.service.network

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.conversation.ChannelChat

interface ChannelService {

    suspend fun getChannels(
        courseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<List<ChannelChat>>

    suspend fun registerInChannel(
        courseId: Long,
        conversationId: Long,
        username: String,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Boolean>
}
