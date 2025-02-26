package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.util

import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.model.Link

object LinkifyService {
    private val urlRegex = """https?:\/\/[^\s<>()\[\]]+""".toRegex()

    fun findLinks(text: String): List<Link> {
        val linkableItems = mutableListOf<Link>()

        val matches = urlRegex.findAll(text)
        for (match in matches) {
            val url = match.value
            val start = match.range.first
            val end = match.range.last + 1

            val isRemoved = text.getOrNull(start - 1) == '<' && text.getOrNull(end) == '>'

            val linkableItem = Link(
                value = url,
                isLink = true,
                start = start,
                end = end,
                isLinkPreviewRemoved = isRemoved
            )

            if (!isRemoved) {
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
            val normalizedUrl = if (!url.endsWith("/")) urlToSearchFor.trimEnd('/') else urlToSearchFor
            val start = match.range.first
            val end = match.range.last + 1

            if (url == normalizedUrl || normalizedUrl.contains(url)) {
                modifiedContent = modifiedContent.substring(0, start) + "<$url>" + modifiedContent.substring(end)
            }
        }

        return modifiedContent
    }
}