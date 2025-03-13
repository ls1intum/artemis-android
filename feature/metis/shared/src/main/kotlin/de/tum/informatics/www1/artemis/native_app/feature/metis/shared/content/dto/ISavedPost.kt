package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto

import kotlinx.serialization.Serializable

interface ISavedPost : IBasePost {
    val referencePostId: Long
    val postingType: SavedPostPostingType
    val savedPostStatus: SavedPostStatus
    val conversation: SimpleConversationInfo

    val key
        get() = "$serverPostId|$postingType"

    @Serializable
    data class SimpleConversationInfo(
        val id: Long,
        val title: String?,
        val type: ConversationType
    ) {
        @Serializable
        enum class ConversationType {
            CHANNEL,
            DIRECT
        }
    }
}