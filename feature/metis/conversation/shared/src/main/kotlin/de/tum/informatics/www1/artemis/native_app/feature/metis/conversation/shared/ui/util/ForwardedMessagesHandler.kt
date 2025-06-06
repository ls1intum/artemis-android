package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.util

import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ForwardedMessage
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.PostingType
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost

/**
 * Handles forwarded messages and loads their source posts, which are acquired in one common request
 * and stored in cachedStandardsSourcePosts and cachedAnswerSourcePosts.
 * Common usage: 1. Extract forwarded messages from a list of posts in [extractForwardedMessages].
 *               2. Load the forwarded messages for the extracted ids and fetch their source posts in [loadForwardedMessages].
 *               3. Match the previously loaded source posts to the destination posts in [resolveForwardedMessages].
 */
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

    /**
     * Extracts all forwarded messages found in loadedPosts and stores their ids in [forwardedPostIds].
     */
    fun extractForwardedMessages(loadedPosts: List<IBasePost>) {
        loadedPosts.forEach { post ->
            if (post.hasForwardedMessages == true) {
                forwardedPostIds.add(post.serverPostId ?: -1)
            }
        }
    }

    /**
     * Loads all forwarded messages for the currently available [forwardedPostIds] and fetches the
     * corresponding source posts by their source ids in [fetchAndCachePosts].
     *
     * @param postingType The type of the destination posts for which the forwarded messages should be loaded.
     */
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

    /**
     * A wrapper method to resolve forwarded messages for a given ChatListItem of type PostItem.ThreadItem.
     * This method is used for the MetisThreadUI only, the logic is handled in [resolveForwardedMessages]
     *
     * @param chatListItem The ChatListItem of type PostItem.ThreadItem for which the source posts should be matched.
     */
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

    /**
     * A wrapper method to resolve forwarded messages for a given ChatListItem of type PostItem.IndexedItem.
     * This method is used for the MetisChatList only, the logic is handled in [resolveForwardedMessages]
     *
     * @param chatListItem The ChatListItem of type PostItem.IndexedItem for which the source posts should be matched.
     */
    fun resolveForwardedMessagesForIndexedPost(
        chatListItem: ChatListItem.PostItem.IndexedItem,
    ): ChatListItem.PostItem.IndexedItem {
        if (chatListItem !is ChatListItem.PostItem.ForwardedMessage) return chatListItem

        return resolveForwardedMessages(chatListItem as ChatListItem.PostItem.IndexedItem.PostWithForwardedMessage) as ChatListItem.PostItem.IndexedItem.PostWithForwardedMessage
    }

    /**
     * A wrapper method to resolve forwarded messages for a given ChatListItem of type PostItem.SavedItem.
     * This method is used for the SavedPosts only, the logic is handled in [resolveForwardedMessages]
     *
     * @param chatListItem The ChatListItem of type PostItem.SavedItem for which the source posts should be matched.
     */
    fun resolveForwardedMessagesForSavedPost(
        chatListItem: ChatListItem.PostItem.SavedItem,
    ): ChatListItem.PostItem.SavedItem {
        if (chatListItem !is ChatListItem.PostItem.ForwardedMessage) return chatListItem

        return resolveForwardedMessages(chatListItem as ChatListItem.PostItem.SavedItem.SavedPostWithForwardedMessage) as ChatListItem.PostItem.SavedItem.SavedPostWithForwardedMessage
    }


    /**
     * Matches the previously loaded source posts to a given destination post by comparing the
     * destination id of the source post with the id of the destination post.
     */
    private fun resolveForwardedMessages(
        chatListItem: ChatListItem.PostItem
    ): ChatListItem.PostItem.ForwardedMessage {
        val forwardedSourcePosts = mutableListOf<IBasePost?>()
        val id =
            cachedForwardedMessages.find { it.destinationPostId == chatListItem.post.serverPostId }?.sourceId
        cachedStandaloneSourcePosts[id]?.let { forwardedSourcePosts += it }
        cachedAnswerSourcePosts[id]?.let { forwardedSourcePosts += it }

        val newChatListItem = chatListItem.copy() as ChatListItem.PostItem.ForwardedMessage
        val oldForwardedPosts = newChatListItem.forwardedPosts
        return newChatListItem.copyWithNewForwardedPosts(oldForwardedPosts + forwardedSourcePosts)
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

            is ChatListItem.PostItem.SavedItem.SavedPostWithForwardedMessage -> copy(
                forwardedPosts = newForwardedPosts
            )

            else -> return this
        }
    }
}