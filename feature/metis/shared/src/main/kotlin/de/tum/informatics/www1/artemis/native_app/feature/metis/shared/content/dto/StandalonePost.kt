package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto

import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class StandalonePost(
    override val id: Long?,
    override val author: User? = null,
    override val authorRole: UserRole? = null,
    override val creationDate: Instant = Clock.System.now(),
    override val updatedDate: Instant? = null,
    override val content: String = "",
    override val reactions: List<Reaction>? = null,
    override val isSaved: Boolean? = null,
    override val title: String? = null,
    val visibleForStudents: Boolean = true,
    override val answers: List<AnswerPost>? = null,
    override val tags: List<String>? = null,
//    val plagiarismCase: PlagiarismCase? = null,
    val conversation: Conversation? = null,
    val courseWideContext: CourseWideContext? = null,
    override val displayPriority: DisplayPriority? = null,
    override val resolved: Boolean? = null,
    override val hasForwardedMessages: Boolean? = null,
    override val forwardedPosts: List<IStandalonePost>? = null,
    override val forwardedAnswerPosts: List<IAnswerPost>? = null,
) : BasePost(), IStandalonePost {

    constructor(post: IStandalonePost, conversation: Conversation) : this(
        id = post.serverPostId,
        author = post.authorId?.let {
            User(
                id = it,
                imageUrl = post.authorImageUrl
            )
        },
        authorRole = post.authorRole,
        content = post.content.orEmpty(),
        conversation = conversation,
        creationDate = post.creationDate ?: Clock.System.now(),
        title = post.title,
        resolved = post.resolved,
        displayPriority = post.displayPriority,
        isSaved = post.isSaved,
        hasForwardedMessages = post.hasForwardedMessages,
        forwardedPosts = post.forwardedPosts,
        forwardedAnswerPosts = post.forwardedAnswerPosts
    )

    @Transient
    override val key: Any = id ?: hashCode()
    
    @Transient
    override val standalonePostId = id?.let(StandalonePostId::ServerSideId)

}
