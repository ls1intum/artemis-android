package de.tum.informatics.www1.artemis.native_app.core.ui.compose

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.ImageFile
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.PdfFile

sealed class LinkBottomSheetState {
    class PDFVIEWSTATE(val pdfFile: PdfFile) : LinkBottomSheetState()
    class IMAGEVIEWSTATE(val imageFile: ImageFile) : LinkBottomSheetState()
    data class WEBVIEWSTATE(val url: String) : LinkBottomSheetState()
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LinkBottomSheet(
    modifier: Modifier,
    state: LinkBottomSheetState,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        modifier = modifier.statusBarsPadding(),
        contentWindowInsets = { WindowInsets.statusBars },
        onDismissRequest = onDismissRequest
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            when (state) {
                is LinkBottomSheetState.PDFVIEWSTATE -> {
                    ArtemisPdfView(
                        modifier = Modifier.fillMaxSize(),
                        pdfFile = state.pdfFile,
                        dismiss = onDismissRequest
                    )
                }

                is LinkBottomSheetState.IMAGEVIEWSTATE -> {
                    ArtemisImageView(
                        modifier = Modifier.fillMaxSize(),
                        state.imageFile,
                        dismiss = onDismissRequest
                    )
                }

                is LinkBottomSheetState.WEBVIEWSTATE -> {
                    var webView: WebView? by remember { mutableStateOf(null) }
                    val webViewState = getWebViewState(state.url)

                    // The lazy column is needed to support scrolling
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxHeight(),
                        state = rememberLazyListState()
                    ) {
                        item {
                            ArtemisWebView(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(8.dp),
                                webViewState = webViewState,
                                webView = webView,
                                setWebView = { webView = it },
                            )
                        }
                    }
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