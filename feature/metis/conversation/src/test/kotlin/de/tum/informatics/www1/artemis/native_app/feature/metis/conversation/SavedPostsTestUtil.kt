package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation

import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ISavedPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostPostingType
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostStatus
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost

object SavedPostsTestUtil {

    private const val DEFAULT_POST_ID = 1L
    private const val DEFAULT_POST_CONTENT = "Default post content"
    private val DEFAULT_CONVERSATION_INFO = ISavedPost.SimpleConversationInfo(
        id = 1L,
        title = "Conversation title",
        type = ISavedPost.SimpleConversationInfo.ConversationType.DIRECT
    )

    fun fromStandalonePost(
        post: StandalonePost,
        status: SavedPostStatus = SavedPostStatus.IN_PROGRESS,
    ): ISavedPost {
        val id = post.serverPostId ?: DEFAULT_POST_ID
        val author = post.author ?: User(
            id = post.authorId ?: 1L,
            name = post.authorName,
            imageUrl = post.authorImageUrl
        )

        return SavedPost(
            id = id,
            postingType = SavedPostPostingType.POST,
            content = post.content,
            savedPostStatus = status,
            author = author,
            conversation = DEFAULT_CONVERSATION_INFO,
            referencePostId = id,
        )
    }

    fun fromAnswer(
        answer: AnswerPost,
        status: SavedPostStatus = SavedPostStatus.IN_PROGRESS,
    ): ISavedPost {
        val author = User(
            id = answer.authorId ?: 1L,
            name = answer.authorName,
            imageUrl = answer.authorImageUrl
        )

        return SavedPost(
            id = answer.serverPostId ?: DEFAULT_POST_ID,
            postingType = SavedPostPostingType.ANSWER,
            content = answer.content ?: DEFAULT_POST_CONTENT,
            savedPostStatus = status,
            author = author,
            conversation = DEFAULT_CONVERSATION_INFO,
            referencePostId = answer.post?.id ?: 0L,
        )
    }
}