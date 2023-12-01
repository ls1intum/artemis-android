package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.Reaction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost

class MetisModificationServiceStub : MetisModificationService {

    companion object {
        private val StubException = RuntimeException("Stub")
    }

    override suspend fun createPost(
        context: MetisContext,
        post: StandalonePost,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<StandalonePost> = NetworkResponse.Failure(StubException)

    override suspend fun updateStandalonePost(
        context: MetisContext,
        post: StandalonePost,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<StandalonePost> = NetworkResponse.Failure(StubException)

    override suspend fun createAnswerPost(
        context: MetisContext,
        post: AnswerPost,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<AnswerPost> = NetworkResponse.Failure(StubException)

    override suspend fun updateAnswerPost(
        context: MetisContext,
        post: AnswerPost,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<AnswerPost> = NetworkResponse.Failure(StubException)

    override suspend fun createReaction(
        context: MetisContext,
        post: MetisModificationService.AffectedPost,
        emojiId: String,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Reaction> = NetworkResponse.Failure(StubException)

    override suspend fun deleteReaction(
        context: MetisContext,
        reactionId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Boolean> = NetworkResponse.Failure(StubException)

    override suspend fun deletePost(
        context: MetisContext,
        post: MetisModificationService.AffectedPost,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Boolean> = NetworkResponse.Failure(StubException)
}
