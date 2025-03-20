package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.post.util

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.PostingType
import kotlinx.serialization.Serializable

@Serializable
data class ForwardedSourcePostContent(
    val sourcePostId: Long,
    val sourcePostType: PostingType
)