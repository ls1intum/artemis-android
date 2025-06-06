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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.WebViewState
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.authTokenOrEmptyString
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.collectArtemisContextAsState
import kotlin.math.roundToInt

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ArtemisWebView(
    modifier: Modifier,
    webViewState: WebViewState,
    webView: WebView?,
    adjustHeightForContent: Boolean = false,
    setWebView: (WebView) -> Unit
) {
    val artemisContext by LocalArtemisContextProvider.current.collectArtemisContextAsState()

    LaunchedEffect(artemisContext) {
        CookieManager.getInstance().setCookie(
            artemisContext.serverUrl,
            "jwt=${artemisContext.authTokenOrEmptyString}"
        )
    }

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val value = if (isSystemInDarkTheme) "DARK" else "LIGHT"
    var webViewHeight by remember { mutableIntStateOf(0) }

    WebView(
        modifier = if (adjustHeightForContent) {
            Modifier
                .fillMaxWidth()
                .height(webViewHeight.dp)
        } else {
            Modifier.fillMaxSize()
        },
        client = remember(value, adjustHeightForContent) {
            ThemeClient(
                value,
                adjustHeightForContent
            ) { height ->
                webViewHeight = height
            }
        },
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

    Box(modifier = modifier) {
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
    private val themeValue: String,
    private val adjustHeightForContent: Boolean,
    private val onHeightChanged: (Int) -> Unit
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

            // The following code is inspired by: https://github.com/ls1intum/artemis-ios/blob/71596a9949bacd29c142cbdfe3a9825d6921628f/ArtemisKit/Sources/CourseView/ExerciseTab/ExerciseDetailViewModel.swift
            // The code can be found in the artemis-ios repository: https://github.com/ls1intum/artemis-ios
            if (adjustHeightForContent) {
                val delay = 750L // This delay is needed to ensure that the page is fully loaded before we try to get the height

                // Set the background color of all divs to transparent to avoid white background
                view.evaluateJavascript(
                    """
                        (function applyBackgroundColorToAllDivs(color) {
                            const allDivs = document.querySelectorAll('div');
                            allDivs.forEach(el => {
                                el.style.backgroundColor = color;
                            });
                        })('transparent');
                    """.trimIndent(),
                    null
                )

                // Get the height of the content after delay
                view.postDelayed({
                    view.evaluateJavascript(
                        """
                        if (document.querySelector("#problem-statement") != null) {
                            document.querySelector("#problem-statement").scrollHeight;
                        } else if (document.querySelector(".instructions__content") != null) {
                            document.querySelector(".instructions__content").scrollHeight;
                        } else {
                            document.body.scrollHeight;
                        }
                    """.trimIndent(),
                    ) { heightString ->
                        val contentHeight = heightString?.toFloatOrNull()?.roundToInt() ?: 0
                        onHeightChanged(contentHeight)
                    }
                }, delay)
            }
        }

        super.onPageFinished(view, url)
    }
}


