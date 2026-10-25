package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.collectArtemisContextAsState
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.PostArtemisMarkdownTransformer
import de.tum.informatics.www1.artemis.native_app.core.ui.media.sameMediaOrigin
import de.tum.informatics.www1.artemis.native_app.core.common.markdown.ArtemisMarkdownTransformer
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.noties.markwon.LinkResolver


@Composable
fun rememberLectureArtemisMarkdownTransformer(serverUrl: String): LectureArtemisMarkdownTransformer {
    val session by LocalArtemisContextProvider.current.collectArtemisContextAsState()
    val courseId = (session as? ArtemisContext.Course)?.courseId
    return remember(serverUrl, courseId) {
        val strippedServerUrl = serverUrl.removeSuffix("/")
        LectureArtemisMarkdownTransformer(strippedServerUrl, courseId)
    }
}


class LectureArtemisMarkdownTransformer(
    private val serverUrl: String,
    private val courseId: Long? = null
) : ArtemisMarkdownTransformer() {

    override fun transformLectureContentMarkdown(type: String, fileName: String, url: String): String {
        val id = courseId ?: return fileName
        return PostArtemisMarkdownTransformer(serverUrl, id).transformMarkdown("[$type]$fileName($url)[/$type]")
    }

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
 * Link resolver for markdown text in the lecture view.
 * If the link is a protected file, it will be opened in a bottom sheet.
 * Otherwise, it will be opened in a browser.
 */
class LectureLinkResolver(
    private val serverUrl: String,
    private val onRequestOpenAttachment: (String) -> Unit,
    private val onRequestOpenLink: (String) -> Unit
) : LinkResolver {
    override fun resolve(view: View, link: String) {
        if (sameMediaOrigin(serverUrl, link) && (link.contains("/api/files/attachments/lecture") || link.contains("/api/core/files/markdown"))) {
            onRequestOpenAttachment(link)
        } else {
            onRequestOpenLink(link)
        }
    }
}
