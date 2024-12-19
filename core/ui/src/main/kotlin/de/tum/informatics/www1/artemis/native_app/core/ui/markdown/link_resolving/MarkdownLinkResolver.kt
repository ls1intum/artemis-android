package de.tum.informatics.www1.artemis.native_app.core.ui.markdown.link_resolving

import androidx.compose.runtime.Composable
import io.noties.markwon.LinkResolver

interface MarkdownLinkResolver {
    @Composable
    fun rememberMarkdownLinkResolver(): LinkResolver
}