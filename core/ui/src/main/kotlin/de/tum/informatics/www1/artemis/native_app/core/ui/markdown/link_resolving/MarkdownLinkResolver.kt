package de.tum.informatics.www1.artemis.native_app.core.ui.markdown.link_resolving

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import io.noties.markwon.LinkResolver

val LocalMarkdownLinkResolver = compositionLocalOf<MarkdownLinkResolver> { EmptyMarkdownLinkResolver() }

interface MarkdownLinkResolver {
    @Composable
    fun rememberMarkdownLinkResolver(): LinkResolver
}

private class EmptyMarkdownLinkResolver : MarkdownLinkResolver {
    @Composable
    override fun rememberMarkdownLinkResolver(): LinkResolver {
        return LinkResolver { view, link ->
            // Do nothing
        }
    }
}