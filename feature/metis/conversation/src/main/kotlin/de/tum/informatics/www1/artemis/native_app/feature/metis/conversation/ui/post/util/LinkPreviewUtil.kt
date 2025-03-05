package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.util

import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.model.Link

object LinkPreviewUtil {
    private val urlRegex = """https?:\/\/[^\s<>()\[\]]+""".toRegex()
    private const val REMOVED_PREVIEW_OPENING_TAG = '<'
    private const val REMOVED_PREVIEW_CLOSING_TAG = '>'

    // This value is currently hardcoded and will be configurable in the future
    // See https://github.com/ls1intum/Artemis/blob/develop/src/main/webapp/app/shared/link-preview/components/link-preview-container/link-preview-container.component.ts
    const val MAX_LINK_PREVIEWS_PER_MESSAGE = 5

    fun generatePreviewableLinks(text: String): List<Link> {
        val linkableItems = mutableListOf<Link>()

        val matches = urlRegex.findAll(text)
        for (match in matches) {
            val url = match.value
            val start = match.range.first
            val end = match.range.last + 1

            val isPreviewRemoved =
                text.getOrNull(start - 1) == REMOVED_PREVIEW_OPENING_TAG && text.getOrNull(end) == REMOVED_PREVIEW_CLOSING_TAG

            val linkableItem = Link(
                value = url,
                isLink = true,
                start = start,
                end = end,
                isLinkPreviewRemoved = isPreviewRemoved
            )

            if (!isPreviewRemoved) {
                linkableItems.add(linkableItem)
            }
        }
        return linkableItems
    }

    fun removeLinkPreview(text: String, urlToSearchFor: String): String {
        var modifiedContent = text
        val matches = urlRegex.findAll(modifiedContent).toList()

        for (match in matches) {
            val url = match.value
            val normalizedUrl =
                if (!url.endsWith("/")) urlToSearchFor.trimEnd('/') else urlToSearchFor
            val start = match.range.first
            val end = match.range.last + 1

            if (url == normalizedUrl || normalizedUrl.contains(url)) {
                modifiedContent = modifiedContent.addRemovedLinkPreviewTags(start, end, url)
            }
        }

        return modifiedContent
    }

    private fun String.addRemovedLinkPreviewTags(start: Int, end: Int, url: String): String =
        this.substring(0, start) + "$REMOVED_PREVIEW_OPENING_TAG$url$REMOVED_PREVIEW_CLOSING_TAG" + this.substring(end)
}