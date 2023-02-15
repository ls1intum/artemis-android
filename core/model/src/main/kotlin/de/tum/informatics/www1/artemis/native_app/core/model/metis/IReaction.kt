package de.tum.informatics.www1.artemis.native_app.core.model.metis

import kotlinx.datetime.Instant

interface IReaction {
    val creationDate: Instant?
    val emojiId: String
    val creatorId: Long?
}