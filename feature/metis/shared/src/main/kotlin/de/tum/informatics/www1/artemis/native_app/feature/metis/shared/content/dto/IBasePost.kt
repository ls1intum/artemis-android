package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto

import androidx.compose.runtime.Stable
import kotlinx.datetime.Instant

@Stable
sealed interface IBasePost {
    val authorName: String?
    val authorId: Long?
    val authorRole: UserRole?
    val authorImageUrl: String?
    val creationDate: Instant?
    val updatedDate: Instant?
    val content: String?
    val reactions: List<IReaction>?
    val isSaved: Boolean?
    val hasForwardedMessages: Boolean?
    val forwardedPosts: List<IStandalonePost>?
    val forwardedAnswerPosts: List<IAnswerPost>?

    val serverPostId: Long?
    val clientPostId: String?
}