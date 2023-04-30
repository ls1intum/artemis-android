package de.tum.informatics.www1.artemis.native_app.feature.metis.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation

interface ConversationService {

    suspend fun getConversations(
        courseId: Long,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<List<Conversation>>

    suspend fun searchForPotentialCommunicationParticipants(
        courseId: Long,
        query: String,
        includeStudents: Boolean,
        includeTutors: Boolean,
        includeInstructors: Boolean,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<List<User>>
}