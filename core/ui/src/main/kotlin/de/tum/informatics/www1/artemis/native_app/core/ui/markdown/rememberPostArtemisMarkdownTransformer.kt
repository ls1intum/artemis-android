package de.tum.informatics.www1.artemis.native_app.core.ui.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberPostArtemisMarkdownTransformer(
    serverUrl: String,
    courseId: Long
): PostArtemisMarkdownTransformer {
    return remember(serverUrl, courseId) {
        val strippedServerUrl =
            if (serverUrl.endsWith("/")) serverUrl.substring(
                0,
                serverUrl.length - 1
            ) else serverUrl

        PostArtemisMarkdownTransformer(serverUrl = strippedServerUrl, courseId = courseId)
    }
}