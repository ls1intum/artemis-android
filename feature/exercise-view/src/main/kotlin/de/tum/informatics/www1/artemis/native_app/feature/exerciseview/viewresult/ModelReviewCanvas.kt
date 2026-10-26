package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.viewresult

import android.annotation.SuppressLint
import android.util.Base64
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.R
import kotlinx.serialization.json.*
import java.io.ByteArrayInputStream

private const val VIEWER_ORIGIN = "https://artemis-model.invalid"
private const val VIEWER_INDEX = "$VIEWER_ORIGIN/model-review/index.html"

/** No server URL, authentication cookie, bearer token or native draft bridge enters this WebView. */
@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun ModelReviewCanvas(model: String?, diagramType: String?, feedback: List<ReviewFeedback>, selectedReference: String?, onSelect: (String) -> Unit) {
    if (model.isNullOrBlank()) { Text(stringResource(R.string.review_model_unavailable)); return }
    var error by remember(model) { mutableStateOf(false) }
    val dark = isSystemInDarkTheme()
    val selection by rememberUpdatedState(onSelect)
    val payload = remember(model, diagramType, feedback, selectedReference, dark) {
        val objectValue = buildJsonObject {
            put("model", runCatching { Json.parseToJsonElement(model) }.getOrElse { JsonNull })
            put("diagramType", diagramType)
            put("selectedReference", selectedReference)
            put("darkMode", dark)
            put("feedback", buildJsonArray {
                feedback.forEach { item -> add(buildJsonObject {
                    item.id?.let { put("id", it.toString()) }
                    put("reference", item.reference)
                    put("text", item.title)
                    put("detailText", item.message)
                    item.credits?.let { put("credits", it) }
                    item.positive?.let { put("positive", it) }
                }) }
            })
        }
        Base64.encodeToString(objectValue.toString().toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }
    val currentPayload by rememberUpdatedState(payload)
    val references by rememberUpdatedState(feedback.mapNotNull { it.reference }.toSet())
    LaunchedEffect(payload) { error = false }
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(520.dp),
        factory = { context ->
            object : WebView(context) {
                override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
                    super.onSizeChanged(w, h, oldw, oldh)
                    if (w > 0 && h > 0) post { renderModelPayload(currentPayload) }
                }
            }.apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                settings.textZoom = 100
                settings.minimumFontSize = 1
                settings.minimumLogicalFontSize = 1
                settings.javaScriptEnabled = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.domStorageEnabled = false
                settings.blockNetworkLoads = true
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
                fun render() = renderModelPayload(currentPayload)
                addJavascriptInterface(object {
                    @JavascriptInterface fun selectElement(reference: String) {
                        if (reference in references) post { selection(reference) }
                    }
                    @JavascriptInterface fun ready() { post { render() } }
                    @JavascriptInterface fun renderError(message: String) { post { error = true } }
                }, "ArtemisModelBridge")
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean = true
                    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse {
                        val uri = request.url
                        val path = uri.path.orEmpty()
                        if (uri.scheme != "https" || uri.host != "artemis-model.invalid" || !path.startsWith("/model-review/") || path.contains("..")) {
                            return WebResourceResponse("text/plain", "UTF-8", 403, "Blocked", emptyMap(), ByteArrayInputStream(ByteArray(0)))
                        }
                        return try {
                            val mime = when (path.substringAfterLast('.')) {
                                "html" -> "text/html"; "js" -> "application/javascript"; "css" -> "text/css"; "woff2" -> "font/woff2"; else -> "application/octet-stream"
                            }
                            WebResourceResponse(mime, "UTF-8", context.assets.open(path.removePrefix("/")))
                        } catch (_: Exception) { WebResourceResponse("text/plain", "UTF-8", 404, "Unavailable", emptyMap(), ByteArrayInputStream(ByteArray(0))) }
                    }
                    override fun onPageFinished(view: WebView, url: String) { if (url == VIEWER_INDEX) render() }
                }
                loadUrl(VIEWER_INDEX)
            }
        },
        update = { view ->
            view.renderModelPayload(payload)
        },
        onRelease = { view -> view.removeJavascriptInterface("ArtemisModelBridge"); view.stopLoading(); view.destroy() },
    )
    if (error) Text(stringResource(R.string.review_model_unavailable))
}

/** Android WebView in a native scrolling pane reports zero CSS percent/vh height despite a measured viewport. */
private fun WebView.renderModelPayload(payload: String) {
    if (width <= 0 || height <= 0) return
    val viewportHeight = (height / resources.displayMetrics.density).toInt().coerceAtLeast(1)
    evaluateJavascript("""
        (function() {
            if (!document.body) return;
            var h = '${viewportHeight}px';
            var resized = document.documentElement.style.height !== h;
            document.documentElement.style.height = h;
            document.body.style.height = h;
            document.documentElement.style.webkitTextSizeAdjust = '100%';
            document.body.style.webkitTextSizeAdjust = '100%';
            var canvas = document.getElementById('canvas');
            if (canvas) canvas.style.height = h;
            if (resized) window.dispatchEvent(new Event('resize'));
            if (window.ArtemisModelReview && window.ArtemisModelReview.isReady) {
                window.ArtemisModelReview.renderBase64('$payload');
            }
        })();
    """.trimIndent(), null)
}
