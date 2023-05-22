package de.tum.informatics.www1.artemis.native_app.feature.metis.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ConversationUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.OneToOneChat

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

    suspend fun createOneToOneConversation(
        courseId: Long,
        partner: String,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<OneToOneChat>

    suspend fun createGroupChat(
        courseId: Long,
        groupMembers: List<String>,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<GroupChat>

    suspend fun createChannel(
        courseId: Long,
        name: String,
        description: String,
        isPublic: Boolean,
        isAnnouncement: Boolean,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<ChannelChat>

    suspend fun updateConversation(
        courseId: Long,
        conversationId: Long,
        newName: String?,
        newDescription: String?,
        newTopic: String?,
        conversation: Conversation,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Boolean>

    suspend fun getMembers(
        courseId: Long,
        conversationId: Long,
        query: String,
        size: Int,
        pageNum: Int,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<List<ConversationUser>>

    suspend fun kickMember(
        courseId: Long,
        conversation: Conversation,
        user: String,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Boolean>

    suspend fun registerMembers(
        courseId: Long,
        conversation: Conversation,
        users: List<String>,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Boolean>

    suspend fun grantModerationRights(
        courseId: Long,
        conversation: Conversation,
        user: String,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Boolean>

    suspend fun revokeModerationRights(
        courseId: Long,
        conversation: Conversation,
        user: String,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Boolean>

    suspend fun archiveChannel(
        courseId: Long,
        conversationId: Long,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Boolean>

    suspend fun unarchiveChannel(
        courseId: Long,
        conversationId: Long,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Boolean>
}

suspend fun ConversationService.getConversation(
    courseId: Long,
    conversationId: Long,
    authToken: String,
    serverUrl: String
): NetworkResponse<Conversation> {
    return getConversations(courseId, authToken, serverUrl)
        .bind { conversations ->
            conversations.firstOrNull { it.id == conversationId }
        }
        .then { conversation ->
            if (conversation != null) {
                NetworkResponse.Response(conversation)
            } else {
                NetworkResponse.Failure(RuntimeException("Conversation not found"))
            }
        }
}
