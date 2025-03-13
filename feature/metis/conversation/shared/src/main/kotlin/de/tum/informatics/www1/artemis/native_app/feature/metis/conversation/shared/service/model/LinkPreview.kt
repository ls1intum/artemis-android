package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.model

import kotlinx.serialization.Serializable

@Serializable
data class LinkPreview(
    val title: String,
    val description: String,
    val image: String,
    val url: String,
    var shouldPreviewBeShown: Boolean = false
) {
    fun isValid(): Boolean {
        return title.isNotEmpty() && description.isNotEmpty() && image.isNotEmpty() && url.isNotEmpty()
    }
}

data class Link(
    val value: String,
    val isLink: Boolean?,
    val start: Int?,
    val end: Int?,
    val isLinkPreviewRemoved: Boolean?
)