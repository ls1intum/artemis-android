package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation

interface ISavedPost : IBasePost {
    val referencePostId: Long
    val savedPostPostingType: SavedPostPostingType
    val savedPostStatus: SavedPostStatus
    val conversation: Conversation
}