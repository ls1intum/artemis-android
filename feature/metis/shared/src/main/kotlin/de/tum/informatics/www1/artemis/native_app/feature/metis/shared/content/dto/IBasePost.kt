package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto

import kotlinx.datetime.Instant

sealed interface IBasePost {
    val authorName: String?
    val authorId: Long?
    val authorRole: UserRole?
    val authorImageUrl: String?
    val creationDate: Instant?
    val updatedDate: Instant?
    val content: String?
    val reactions: List<IReaction>?

    val serverPostId: Long?
    val clientPostId: String?
}