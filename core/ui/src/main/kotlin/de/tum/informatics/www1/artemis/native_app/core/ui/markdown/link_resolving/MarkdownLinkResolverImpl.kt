package de.tum.informatics.www1.artemis.native_app.core.ui.markdown.link_resolving

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.LinkBottomSheet
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.LinkBottomSheetState
import io.noties.markwon.LinkResolver

val LocalMarkdownLinkResolver = compositionLocalOf<MarkdownLinkResolver> { error("No MarkdownLinkResolver provided") }

class MarkdownLinkResolverImpl(
    private val accountService: AccountService,
    private val serverConfigurationService: ServerConfigurationService,
): MarkdownLinkResolver {
    @Composable
    override fun rememberMarkdownLinkResolver(): LinkResolver {
        val serverUrl by serverConfigurationService.serverUrl.collectAsState(initial = "")
        val authToken by accountService.authToken.collectAsState(initial = "")
        val context = LocalContext.current

        val (bottomSheetLink, setLinkToShow) = remember { mutableStateOf<String?>(null) }
        val (bottomSheetState, setBottomSheetState) = remember { mutableStateOf(LinkBottomSheetState.WEBVIEWSTATE) }

        if (bottomSheetLink != null) {
            val filename = if (bottomSheetState == LinkBottomSheetState.PDFVIEWSTATE) bottomSheetLink.substringAfterLast("/") else null
            LinkBottomSheet(
                modifier = Modifier.fillMaxSize(),
                serverUrl = serverUrl,
                authToken = authToken,
                link = bottomSheetLink,
                fileName = filename,
                state = bottomSheetState,
                onDismissRequest = { setLinkToShow(null) }
            )
        }

        return remember(context, authToken, serverUrl) {
            BaseMarkdownLinkResolver(context, serverUrl, setLinkToShow, setBottomSheetState)
        }
    }
}

class BaseMarkdownLinkResolver(
    private val context: Context,
    private val serverUrl: String = "",
    private val showModalBottomSheet: (String) -> Unit,
    private val setBottomSheetState: (LinkBottomSheetState) -> Unit
) : LinkResolver {
    override fun resolve(view: View, link: String) {
        when {
            link.endsWith(".pdf") -> {
                setBottomSheetState(LinkBottomSheetState.PDFVIEWSTATE)
                showModalBottomSheet(link)
            }
            // TODO: open Artemis link in a Modal Bottom Sheet webview to attach session cookie (https://github.com/ls1intum/artemis-android/issues/245)
//            link.startsWith(serverUrl) -> {
//                setBottomSheetState(LinkBottomSheetState.WEBVIEWSTATE)
//                showModalBottomSheet(link)
//            }
            else -> {
                val customTabsIntent = CustomTabsIntent.Builder().build()
                customTabsIntent.launchUrl(context, Uri.parse(link))
            }
        }
    }
}
