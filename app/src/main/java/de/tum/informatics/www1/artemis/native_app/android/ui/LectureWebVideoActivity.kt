package de.tum.informatics.www1.artemis.native_app.android.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import de.tum.informatics.www1.artemis.native_app.android.ui.theme.AppTheme
import de.tum.informatics.www1.artemis.native_app.core.ui.media.mediaHttpUri
import de.tum.informatics.www1.artemis.native_app.core.ui.R as CoreR

/** Non-exported, separate process + WebView data directory; Artemis credentials never enter it. */
class LectureWebVideoActivity : ComponentActivity() {
    companion object { const val URL_EXTRA = "lecture_video_url" }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val url = intent.getStringExtra(URL_EXTRA).orEmpty()
        if (mediaHttpUri(url) == null) { finish(); return }
        // Only this isolated process's cookie manager is affected; login's cookie store is untouched.
        CookieManager.getInstance().setAcceptCookie(false)
        setContent {
            AppTheme {
                Surface(Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(12.dp)) {
                        TextButton(onClick = ::finish, modifier = Modifier.align(Alignment.End)) {
                            Text(stringResource(CoreR.string.lecture_media_close))
                        }
                        ProviderPage(url, Modifier.weight(1f))
                        Text(stringResource(CoreR.string.lecture_media_provider), style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { CustomTabsIntent.Builder().build().launchUrl(this@LectureWebVideoActivity, url.toUri()) }) {
                            Text(stringResource(CoreR.string.lecture_media_browser))
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ProviderPage(url: String, modifier: Modifier) {
    var attempt by remember { mutableIntStateOf(0) }
    key(attempt) {
        var failed by remember { mutableStateOf(false) }
        var loading by remember { mutableStateOf(true) }
        var webView by remember { mutableStateOf<WebView?>(null) }
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        var foreground by remember { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) }
        DisposableEffect(lifecycle) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP) foreground = false
                if (event == Lifecycle.Event.ON_START) foreground = true
                if (event == Lifecycle.Event.ON_PAUSE) {
                    webView?.evaluateJavascript("document.querySelectorAll('video,audio').forEach(function(m){m.pause();});", null)
                    webView?.onPause()
                }
            }
            lifecycle.addObserver(observer)
            onDispose { lifecycle.removeObserver(observer) }
        }
        LaunchedEffect(url) {
            kotlinx.coroutines.delay(20_000)
            if (loading) { failed = true; loading = false }
        }
        Box(modifier) {
            if (foreground) AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context -> WebView(context).apply {
                    webView = this
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.mediaPlaybackRequiresUserGesture = true
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                            val invalid = mediaHttpUri(request.url.toString()) == null
                            if (invalid && request.isForMainFrame) { failed = true; loading = false }
                            return invalid
                        }
                        override fun onPageStarted(view: WebView, target: String, icon: Bitmap?) { loading = true }
                        override fun onPageFinished(view: WebView, target: String) { loading = false }
                        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                            if (request.isForMainFrame) { failed = true; loading = false }
                        }
                        override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, response: WebResourceResponse) {
                            if (request.isForMainFrame && response.statusCode >= 400) { failed = true; loading = false }
                        }
                        override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
                            failed = true; loading = false
                            view.destroy(); webView = null
                            return true
                        }
                    }
                    loadUrl(url)
                } },
                onRelease = { it.stopLoading(); it.onPause(); it.destroy(); webView = null }
            )
            if (loading && !failed) CircularProgressIndicator(Modifier.align(Alignment.Center))
            if (failed) Surface(Modifier.fillMaxSize()) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.Center) {
                    Text(stringResource(CoreR.string.lecture_media_failed))
                    Button(onClick = { attempt++ }) { Text(stringResource(CoreR.string.lecture_media_retry)) }
                }
            }
        }
    }
}
