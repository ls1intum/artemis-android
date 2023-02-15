package de.tum.informatics.www1.artemis.native_app.core.model.metis

import kotlinx.datetime.Instant

interface IBasePost {
    val authorName: String?
    val authorRole: UserRole?
    val creationDate: Instant?
    val content: String?
    val reactions: List<IReaction>?
}