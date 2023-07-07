package de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto

import kotlinx.datetime.Instant

sealed interface IBasePost {
    val authorName: String?
    val authorId: Long?
    val authorRole: UserRole?
    val creationDate: Instant?
    val updatedDate: Instant?
    val content: String?
    val reactions: List<IReaction>?
}