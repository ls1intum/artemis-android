package de.tum.informatics.www1.artemis.native_app.feature.exerciseview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.google.accompanist.web.WebContent
import com.google.accompanist.web.WebViewState
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ResultTemplateStatus
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments

@Composable
internal fun getProblemStatementWebViewState(
    serverUrl: String,
    courseId: Long?,
    exerciseId: Long?,
    participationId: Long?
): WebViewState? {
    val url by remember(serverUrl, courseId, exerciseId) {
        derivedStateOf {
            if (courseId != null && exerciseId != null) {
                URLBuilder(serverUrl).apply {
                    appendPathSegments(
                        "courses",
                        courseId.toString(),
                        "exercises",
                        exerciseId.toString(),
                        "problem-statement"
                    )

                    if (participationId != null) {
                        appendPathSegments(participationId.toString())
                    }
                }
                    .buildString()
            } else null
        }
    }

    return remember(url) {
        derivedStateOf {
            url?.let {
                WebViewState(WebContent.Url(url = it))
            }
        }
    }.value
}

@Composable
internal fun getFeedbackViewWebViewState(
    serverUrl: String,
    courseId: Long,
    exerciseId: Long,
    participationId: Long,
    resultId: Long,
    templateStatus: ResultTemplateStatus
): WebViewState {
    val url by remember(serverUrl, courseId, exerciseId, resultId, templateStatus) {
        derivedStateOf {
            URLBuilder(serverUrl).apply {
                appendPathSegments(
                    "courses",
                    courseId.toString(),
                    "exercises",
                    exerciseId.toString(),
                    "participations",
                    participationId.toString(),
                    "results",
                    resultId.toString(),
                    "feedback"
                )
            }.buildString()
        }
    }

    return remember(url) {
        derivedStateOf {
            url.let {
                WebViewState(WebContent.Url(url = it))
            }
        }
    }.value
}