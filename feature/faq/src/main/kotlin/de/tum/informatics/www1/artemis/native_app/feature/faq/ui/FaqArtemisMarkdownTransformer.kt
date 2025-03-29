package de.tum.informatics.www1.artemis.native_app.feature.faq.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.tum.informatics.www1.artemis.native_app.core.common.markdown.ArtemisMarkdownTransformer
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments


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
            appendPathSegments(*Api.Core.UploadedFile.path)
            appendPathSegments(filePath)
        }.buildString()
        println("File upload message markdown: $url")
        val link = "[$fileName]($url)"
        return if (isImage) "!$link" else link
    }
}