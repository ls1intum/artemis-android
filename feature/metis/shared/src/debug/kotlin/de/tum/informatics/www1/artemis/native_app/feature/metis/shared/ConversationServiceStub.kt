package de.tum.informatics.www1.artemis.native_app.feature.metis.shared

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ConversationUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService

open class ConversationServiceStub(
    private val conversations: List<Conversation>
) :
    ConversationService {

    companion object {
        private val StubException = RuntimeException("Stub")
    }

    override suspend fun getConversations(
        courseId: Long,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<List<Conversation>> = NetworkResponse.Response(conversations)

    override suspend fun searchForPotentialCommunicationParticipants(
        courseId: Long,
        query: String,
        includeStudents: Boolean,
        includeTutors: Boolean,
        includeInstructors: Boolean,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<List<User>> = NetworkResponse.Response(emptyList())

    override suspend fun createOneToOneConversation(
        courseId: Long,
        partner: String,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<OneToOneChat> = NetworkResponse.Failure(StubException)

    override suspend fun createGroupChat(
        courseId: Long,
        groupMembers: List<String>,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<GroupChat> = NetworkResponse.Failure(StubException)

    override suspend fun createChannel(
        courseId: Long,
        name: String,
        description: String,
        isPublic: Boolean,
        isAnnouncement: Boolean,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<ChannelChat> = NetworkResponse.Failure(StubException)

    override suspend fun updateConversation(
        courseId: Long,
        conversationId: Long,
        newName: String?,
        newDescription: String?,
        newTopic: String?,
        conversation: Conversation,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Boolean> = NetworkResponse.Failure(StubException)

    override suspend fun getMembers(
        courseId: Long,
        conversationId: Long,
        query: String,
        size: Int,
        pageNum: Int,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<List<ConversationUser>> = NetworkResponse.Failure(StubException)

    override suspend fun kickMember(
        courseId: Long,
        conversation: Conversation,
        user: String,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Boolean> = NetworkResponse.Failure(StubException)

    override suspend fun registerMembers(
        courseId: Long,
        conversation: Conversation,
        users: List<String>,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Boolean> = NetworkResponse.Failure(StubException)

    override suspend fun grantModerationRights(
        courseId: Long,
        conversation: Conversation,
        user: String,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Boolean> = NetworkResponse.Failure(StubException)

    override suspend fun revokeModerationRights(
        courseId: Long,
        conversation: Conversation,
        user: String,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Boolean> = NetworkResponse.Failure(StubException)

    override suspend fun archiveChannel(
        courseId: Long,
        conversationId: Long,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Boolean> = NetworkResponse.Failure(StubException)

    override suspend fun unarchiveChannel(
        courseId: Long,
        conversationId: Long,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Boolean> = NetworkResponse.Failure(StubException)

    override suspend fun markConversationAsHidden(
        courseId: Long,
        conversationId: Long,
        hidden: Boolean,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Boolean> = NetworkResponse.Failure(StubException)

    override suspend fun markConversationAsFavorite(
        courseId: Long,
        conversationId: Long,
        favorite: Boolean,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Boolean> = NetworkResponse.Failure(StubException)

    override suspend fun markConversationMuted(
        courseId: Long,
        conversationId: Long,
        muted: Boolean,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Boolean> = NetworkResponse.Failure(StubException)
}
