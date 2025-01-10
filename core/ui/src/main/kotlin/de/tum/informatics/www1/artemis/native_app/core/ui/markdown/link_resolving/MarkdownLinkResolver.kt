package de.tum.informatics.www1.artemis.native_app.core.ui.markdown.link_resolving

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import io.noties.markwon.LinkResolver

val LocalMarkdownLinkResolver = compositionLocalOf<MarkdownLinkResolver> { error("No MarkdownLinkResolver provided") }

interface MarkdownLinkResolver {
    @Composable
    fun rememberMarkdownLinkResolver(): LinkResolver
}