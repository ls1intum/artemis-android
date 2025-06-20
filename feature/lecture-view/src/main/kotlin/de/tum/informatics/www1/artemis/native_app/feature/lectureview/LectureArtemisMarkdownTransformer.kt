package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.tum.informatics.www1.artemis.native_app.core.common.markdown.ArtemisMarkdownTransformer
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments


@Composable
fun rememberLectureArtemisMarkdownTransformer(serverUrl: String): LectureArtemisMarkdownTransformer {
    return remember(serverUrl) {
        val strippedServerUrl = serverUrl.removeSuffix("/")
        LectureArtemisMarkdownTransformer(strippedServerUrl)
    }
}


class LectureArtemisMarkdownTransformer(
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