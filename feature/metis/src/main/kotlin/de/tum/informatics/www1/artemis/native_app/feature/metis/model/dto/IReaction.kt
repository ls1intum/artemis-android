package de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto

import kotlinx.datetime.Instant

interface IReaction {
    val creationDate: Instant?
    val emojiId: String
    val creatorId: Long?
}