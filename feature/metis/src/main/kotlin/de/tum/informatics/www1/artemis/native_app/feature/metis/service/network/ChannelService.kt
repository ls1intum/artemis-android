package de.tum.informatics.www1.artemis.native_app.feature.metis.service.network

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat

interface ChannelService {

    suspend fun getChannels(
        courseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<List<de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat>>

    suspend fun registerInChannel(
        courseId: Long,
        conversationId: Long,
        username: String,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Boolean>
}
