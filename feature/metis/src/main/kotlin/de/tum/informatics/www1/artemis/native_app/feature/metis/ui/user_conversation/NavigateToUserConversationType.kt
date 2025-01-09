package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.user_conversation

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.model.account.BaseAccount
import de.tum.informatics.www1.artemis.native_app.feature.metis.NavigateToUserConversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.NavigateToUserConversationById
import de.tum.informatics.www1.artemis.native_app.feature.metis.NavigateToUserConversationByUsername
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService

sealed class NavigateToUserConversationType {

    abstract fun isRequestedAccount(account: BaseAccount): Boolean

    abstract suspend fun createConversation(
        conversationService: ConversationService,
        courseId: Long,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<OneToOneChat>

    data class ByUsername(
        val username: String
    ) : NavigateToUserConversationType() {
        override fun isRequestedAccount(account: BaseAccount): Boolean {
            return account.username == username
        }

        override suspend fun createConversation(
            conversationService: ConversationService,
            courseId: Long,
            authToken: String,
            serverUrl: String
        ): NetworkResponse<OneToOneChat> {
            return conversationService.createOneToOneConversation(courseId, username, authToken, serverUrl)
        }
    }

    data class ById(
        val userId: Long
    ) : NavigateToUserConversationType() {
        override fun isRequestedAccount(account: BaseAccount): Boolean {
            return account.id == userId
        }

        override suspend fun createConversation(
            conversationService: ConversationService,
            courseId: Long,
            authToken: String,
            serverUrl: String
        ): NetworkResponse<OneToOneChat> {
            return conversationService.createOneToOneConversation(courseId, userId, authToken, serverUrl)
        }
    }

    companion object {
        fun from(navigationToUserConversation: NavigateToUserConversation): NavigateToUserConversationType {
            return when (navigationToUserConversation) {
                is NavigateToUserConversationByUsername -> ByUsername(navigationToUserConversation.username)
                is NavigateToUserConversationById -> ById(navigationToUserConversation.userId)
            }
        }
    }
}