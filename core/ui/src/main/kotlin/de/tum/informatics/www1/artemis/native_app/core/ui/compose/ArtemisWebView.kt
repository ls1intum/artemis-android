package de.tum.informatics.www1.artemis.native_app.core.ui.compose

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.WebViewState

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ArtemisWebView(
    modifier: Modifier,
    webViewState: WebViewState,
    webView: WebView?,
    serverUrl: String,
    authToken: String,
    setWebView: (WebView) -> Unit
) {
    LaunchedEffect(serverUrl, authToken) {
        CookieManager.getInstance().setCookie(serverUrl, "jwt=$authToken")
    }

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val value = if (isSystemInDarkTheme) "DARK" else "LIGHT"

    Box(modifier = modifier) {
        WebView(
            modifier = Modifier.fillMaxSize(),
            client = remember(value) { ThemeClient(value) },
            state = webViewState,
            onCreated = {
                it.settings.javaScriptEnabled = true
                it.settings.domStorageEnabled = true
                it.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                it.setBackgroundColor(Color.TRANSPARENT)
            },
            factory = { context ->
                if (webView != null) {
                    (webView.parent as? ViewGroup)?.removeView(webView)
                    webView
                } else {
                    val newWebView = ArtemisWebViewImpl(context)
                    setWebView(newWebView)
                    newWebView
                }
            }
        )

        if (webViewState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

private class ArtemisWebViewImpl(context: Context) : WebView(context) {

    private var prevLoadedUrl: String? = null

    override fun loadUrl(url: String, additionalHttpHeaders: MutableMap<String, String>) {
        if (prevLoadedUrl != url) {
            prevLoadedUrl = url
            super.loadUrl(url, additionalHttpHeaders)
        }
    }
}

private class ThemeClient(
    private val themeValue: String
) : AccompanistWebViewClient() {
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        if (view != null) {
            setLocalStorage(view)
        }

        super.onPageStarted(view, url, favicon)
    }


    private fun setLocalStorage(view: WebView) {
        view.evaluateJavascript(
            """
                localStorage.setItem('jhi-artemisapp.theme.preference', '"$themeValue"');
            """.trimIndent(), null
        )
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        if (view != null) {
            setLocalStorage(view)
        }

        super.onPageFinished(view, url)
    }
}


