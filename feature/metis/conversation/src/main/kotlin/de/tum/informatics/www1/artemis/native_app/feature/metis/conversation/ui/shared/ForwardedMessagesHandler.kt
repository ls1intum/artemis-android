package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.shared

import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
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
    private val forwardedPostIds = mutableListOf<Long>()
    private var cachedForwardedMessages: List<ForwardedMessage> = emptyList()
    private var cachedStandaloneSourcePosts: Map<Long, List<StandalonePost?>> = mapOf()
    private var cachedAnswerSourcePosts: Map<Long, List<AnswerPost?>> = mapOf()

    fun resolveForwardedMessagesForThreadPost(chatListItem: ChatListItem.PostItem.ThreadItem): ChatListItem.PostItem.ThreadItem {
        if (chatListItem !is ChatListItem.PostItem.ForwardedMessage) return chatListItem

        return when (chatListItem) {
            is ChatListItem.PostItem.ThreadItem.Answer.AnswerPostWithForwardedMessage ->
                resolveForwardedMessages(chatListItem) as ChatListItem.PostItem.ThreadItem.Answer.AnswerPostWithForwardedMessage

            is ChatListItem.PostItem.ThreadItem.ContextItem.ContextPostWithForwardedMessage ->
                resolveForwardedMessages(chatListItem) as ChatListItem.PostItem.ThreadItem.ContextItem.ContextPostWithForwardedMessage

            else -> return chatListItem
        }
    }

    fun resolveForwardedMessagesForIndexedPost(
        chatListItem: ChatListItem.PostItem.IndexedItem,
    ): ChatListItem.PostItem.IndexedItem {
        if (chatListItem !is ChatListItem.PostItem.ForwardedMessage) return chatListItem

        return resolveForwardedMessages(chatListItem as ChatListItem.PostItem.IndexedItem.PostWithForwardedMessage) as ChatListItem.PostItem.IndexedItem.PostWithForwardedMessage
    }

    private fun resolveForwardedMessages(
        chatListItem: ChatListItem.PostItem
    ): ChatListItem.PostItem.ForwardedMessage {
        var forwardedSourcePosts = listOf<IBasePost?>()
        val id =
            cachedForwardedMessages.find { it.destinationPostId == chatListItem.post.serverPostId }?.sourceId
        forwardedSourcePosts = forwardedSourcePosts + (cachedStandaloneSourcePosts[id] ?: emptyList())
        forwardedSourcePosts = forwardedSourcePosts + (cachedAnswerSourcePosts[id] ?: emptyList())

        val newChatListItem = chatListItem.copy() as ChatListItem.PostItem.ForwardedMessage
        val oldForwardedPosts = newChatListItem.forwardedPosts
        return newChatListItem.copyWithNewForwardedPosts(oldForwardedPosts + forwardedSourcePosts)
    }

    suspend fun loadForwardedMessages(postingType: PostingType) {
        metisService.getForwardedMessagesByIds(
            metisContext = metisContext,
            postIds = forwardedPostIds,
            postType = postingType,
            serverUrl = serverUrl,
            authToken = authToken
        ).bind { forwardedMessages ->
            cachedForwardedMessages =
                (cachedForwardedMessages + forwardedMessages).distinctBy { it.id }

            val (sourcePostIds, sourceAnswerPostIds) = forwardedMessages.partition { it.sourceType == PostingType.POST }
                .let { it.first.mapNotNull { msg -> msg.sourceId } to it.second.mapNotNull { msg -> msg.sourceId } }

            fetchAndCachePosts(sourcePostIds, sourceAnswerPostIds)
        }
    }

    fun extractForwardedMessages(loadedPosts: List<IBasePost>) {
        loadedPosts.forEach { post ->
            if (post.hasForwardedMessages == true) {
                forwardedPostIds.add(post.serverPostId ?: -1)
            }
        }
    }

    private suspend fun fetchAndCachePosts(
        sourcePostIds: List<Long>,
        sourceAnswerPostIds: List<Long>
    ) {
        if (sourcePostIds.isNotEmpty()) {
            metisService.getPostsByIds(
                metisContext = metisContext,
                postIds = sourcePostIds,
                serverUrl = serverUrl,
                authToken = authToken
            ).bind { sourcePosts ->
                val updatedSourcePosts = sourcePostIds.associateWith { id ->
                    val posts = sourcePosts.filter { it.serverPostId == id }
                    posts.ifEmpty { listOf(null) } // pairs id with the found post or null for missing posts
                }

                cachedStandaloneSourcePosts = updatedSourcePosts
            }
        }

        if (sourceAnswerPostIds.isNotEmpty()) {
            metisService.getAnswerPostsByIds(
                metisContext = metisContext,
                answerPostIds = sourceAnswerPostIds,
                serverUrl = serverUrl,
                authToken = authToken
            ).bind { sourceAnswerPosts ->
                val updatedSourceAnswerPosts = sourceAnswerPostIds.associateWith { id ->
                    val posts = sourceAnswerPosts.filter { it.serverPostId == id }
                    posts.ifEmpty { listOf(null) } // pairs id with the found post or null for missing posts
                }

                cachedAnswerSourcePosts = updatedSourceAnswerPosts
            }
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