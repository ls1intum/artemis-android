package de.tum.informatics.www1.artemis.native_app.feature.exercise_view

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.accompanist.web.WebContent
import com.google.accompanist.web.WebView
import com.google.accompanist.web.WebViewState
import io.ktor.http.*

@Composable
internal fun getProblemStatementWebViewState(
    serverUrl: String,
    courseId: Long?,
    exerciseId: Long?
): WebViewState? {
    val url by remember(serverUrl, courseId, exerciseId) {
        derivedStateOf {
            if (courseId != null && exerciseId != null) {
                URLBuilder(serverUrl).apply {
                    appendPathSegments(
                        "courses",
                        courseId.toString(),
                        "exercises",
                        exerciseId.toString()
                    )
                }
                    .buildString()
            } else null
        }
    }

    return remember(url) {
        derivedStateOf {
            val currentUrl = url
            if (currentUrl != null) {
                WebViewState(WebContent.Url(url = currentUrl))
            } else null
        }
    }.value
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun ProblemStatementWebView(
    modifier: Modifier,
    webViewState: WebViewState,
    webView: WebView?,
    setWebView: (WebView) -> Unit
) {
    WebView(
        modifier = modifier,
        state = webViewState,
        onCreated = {
            it.settings.javaScriptEnabled = true
            it.settings.domStorageEnabled = true
            it.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        },
        factory = { context ->
            if (webView != null) {
                webView
            } else {
                val newWebView = WebView(context)
                setWebView(newWebView)
                newWebView
            }
        }
    )
}