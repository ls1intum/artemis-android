package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.shared

import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ForwardedMessage
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.PostingType
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost

class ForwardedMessagesHandler(
    private val metisService: MetisService,
    private val metisContext: MetisContext,
    private val authToken: String,
    private val serverUrl: String
) {
    val forwardedPostIds = mutableListOf<Long>()
    private var cachedForwardedMessages: List<ForwardedMessage> = emptyList()
    private var standaloneSourcePosts: List<StandalonePost?> = emptyList()

    suspend fun resolveForwardedMessagesForThreadPost(chatListItem: ChatListItem.PostItem.ThreadItem): ChatListItem.PostItem.ThreadItem {
        if (chatListItem !is ChatListItem.PostItem.ForwardedMessage) return chatListItem

        return when (chatListItem) {
            is ChatListItem.PostItem.ThreadItem.Answer.AnswerPostWithForwardedMessage ->
                resolveForwardedMessages(chatListItem) as ChatListItem.PostItem.ThreadItem.Answer.AnswerPostWithForwardedMessage

            is ChatListItem.PostItem.ThreadItem.ContextItem.ContextPostWithForwardedMessage ->
                resolveForwardedMessages(chatListItem) as ChatListItem.PostItem.ThreadItem.ContextItem.ContextPostWithForwardedMessage

            else -> return chatListItem
        }
    }

    suspend fun resolveForwardedMessagesForIndexedPost(
        chatListItem: ChatListItem.PostItem.IndexedItem,
    ): ChatListItem.PostItem.IndexedItem {
        if (chatListItem !is ChatListItem.PostItem.ForwardedMessage) return chatListItem

        return resolveForwardedMessages(chatListItem as ChatListItem.PostItem.IndexedItem.PostWithForwardedMessage) as ChatListItem.PostItem.IndexedItem.PostWithForwardedMessage
    }

    private suspend fun resolveForwardedMessages(
        chatListItem: ChatListItem.PostItem
    ): ChatListItem.PostItem.ForwardedMessage {
        val sourcePostIds = mutableListOf<Long>()
        val sourceAnswerPostIds = mutableListOf<Long>()
        var modifiedChatListItem = chatListItem.copy() as ChatListItem.PostItem.ForwardedMessage

        val relevantForwardedMessages = cachedForwardedMessages.filter {
            it.destinationPostId == chatListItem.post.serverPostId
        }

        relevantForwardedMessages.forEach { forwardedMessage ->
            forwardedMessage.sourceId?.let { sourceId ->
                if (forwardedMessage.sourceType == PostingType.POST) {
                    sourcePostIds.add(sourceId)
                } else {
                    sourceAnswerPostIds.add(sourceId)
                }
            }
        }

        if (sourcePostIds.isNotEmpty()) {
            metisService.getPostsByIds(
                metisContext = metisContext,
                postIds = sourcePostIds,
                serverUrl = serverUrl,
                authToken = authToken
            ).bind { sourcePosts ->
                val updatedSourcePosts = sourcePostIds.map { id ->
                    sourcePosts.find { it.serverPostId == id } // inserts null for missing posts
                }

                standaloneSourcePosts = updatedSourcePosts
                val oldForwardedPosts = modifiedChatListItem.forwardedPosts
                modifiedChatListItem =
                    modifiedChatListItem.copyWithNewForwardedPosts(oldForwardedPosts + updatedSourcePosts)
            }
        }

        if (sourceAnswerPostIds.isNotEmpty()) {
            metisService.getAnswerPostsByIds(
                metisContext = metisContext,
                answerPostIds = sourceAnswerPostIds,
                serverUrl = serverUrl,
                authToken = authToken
            ).bind { sourceAnswerPosts ->
                val updatedSourceAnswerPosts = sourceAnswerPostIds.map { id ->
                    sourceAnswerPosts.find { it.serverPostId == id } // inserts null for missing posts
                }

                val oldForwardedPosts = modifiedChatListItem.forwardedPosts
                modifiedChatListItem =
                    modifiedChatListItem.copyWithNewForwardedPosts(oldForwardedPosts + updatedSourceAnswerPosts)
            }
        }

        sourcePostIds.clear()
        sourceAnswerPostIds.clear()

        return modifiedChatListItem
    }

    suspend fun loadForwardedMessages(postingType: PostingType) {
        metisService.getForwardedMessagesByIds(
            metisContext = metisContext,
            postIds = forwardedPostIds,
            postType = postingType,
            serverUrl = serverUrl,
            authToken = authToken
        ).bind { forwardedMessages ->
            cachedForwardedMessages = cachedForwardedMessages + forwardedMessages
            cachedForwardedMessages = cachedForwardedMessages.distinctBy { it.id }
        }
    }

    private fun ChatListItem.PostItem.ForwardedMessage.copyWithNewForwardedPosts(newForwardedPosts: List<IBasePost?>): ChatListItem.PostItem.ForwardedMessage {
        return when (this) {
            is ChatListItem.PostItem.IndexedItem.PostWithForwardedMessage -> copy(forwardedPosts = newForwardedPosts)
            is ChatListItem.PostItem.ThreadItem.Answer.AnswerPostWithForwardedMessage -> copy(
                forwardedPosts = newForwardedPosts
            )

            is ChatListItem.PostItem.ThreadItem.ContextItem.ContextPostWithForwardedMessage -> copy(
                forwardedPosts = newForwardedPosts
            )

            else -> return this
        }
    }
}