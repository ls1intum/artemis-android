package de.tum.informatics.www1.artemis.native_app.feature.faq.ui

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.tum.informatics.www1.artemis.native_app.core.common.markdown.ArtemisMarkdownTransformer
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.noties.markwon.LinkResolver


@Composable
fun rememberFaqArtemisMarkdownTransformer(serverUrl: String): FaqArtemisMarkdownTransformer {
    return remember(serverUrl) {
        val strippedServerUrl = serverUrl.removeSuffix("/")
        FaqArtemisMarkdownTransformer(strippedServerUrl)
    }
}


class FaqArtemisMarkdownTransformer(
    private val serverUrl: String
) : ArtemisMarkdownTransformer() {

    override fun transformFileUploadMessageMarkdown(
        isImage: Boolean,
        fileName: String,
        filePath: String
    ): String {
        val url = URLBuilder(serverUrl).apply {
            appendPathSegments(filePath)
        }.buildString()
        val link = "[$fileName]($url)"
        return if (isImage) "!$link" else link
    }
}

/**
 * Link resolver for markdown text in the faq view.
 * If the link is a protected file, it will be opened in a bottom sheet.
 * Otherwise, it will be opened in a browser.
 */
class FaqLinkResolver(
    private val serverUrl: String,
    private val onRequestOpenAttachment: (String) -> Unit,
    private val onRequestOpenLink: (String) -> Unit
) : LinkResolver {
    override fun resolve(view: View, link: String) {
        if (link.startsWith(serverUrl) && link.contains("/api/core/files/markdown")) {
            onRequestOpenAttachment(link)
        } else {
            onRequestOpenLink(link)
        }
    }
}