package de.tum.informatics.www1.artemis.native_app.core.ui.compose

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.web.WebContent
import com.google.accompanist.web.WebViewState

enum class LinkBottomSheetState {
    PDFVIEWSTATE,
    WEBVIEWSTATE
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LinkBottomSheet(
    modifier: Modifier,
    serverUrl: String,
    authToken: String,
    link: String,
    state: LinkBottomSheetState,
    onDismissRequest: () -> Unit
) {
    var webView: WebView? by remember { mutableStateOf(null) }
    val webViewState = getWebViewState(link)

    ModalBottomSheet (
        modifier = modifier,
        onDismissRequest = onDismissRequest
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(8.dp)
        ) {
                when (state) {
                    LinkBottomSheetState.PDFVIEWSTATE -> {

                    }
                    LinkBottomSheetState.WEBVIEWSTATE -> {
                        ArtemisWebView(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(8.dp),
                            webViewState = webViewState,
                            webView = webView,
                            setWebView = { webView = it },
                            serverUrl = serverUrl,
                            authToken = authToken
                        )
                    }
                }
            }

    }
}

@Composable
private fun getWebViewState(
    link: String
): WebViewState {
    return remember(link) {
        derivedStateOf {
            WebViewState(WebContent.Url(url = link))
        }
    }.value
}