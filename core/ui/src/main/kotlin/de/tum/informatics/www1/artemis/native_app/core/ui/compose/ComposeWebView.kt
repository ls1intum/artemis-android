package de.tum.informatics.www1.artemis.native_app.core.ui.compose

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ComposeWebView(
    modifier: Modifier,
    url: String,
    webViewClient: WebViewClient = WebViewClient()
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true

                loadUrl(url)
            }
        },
        update = { webView ->
            webView.webViewClient = webViewClient
        }
    )
}