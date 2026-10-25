package de.tum.informatics.www1.artemis.native_app.core.ui.markdown.link_resolving

import de.tum.informatics.www1.artemis.native_app.core.ui.media.*
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.authTokenOrEmptyString
import de.tum.informatics.www1.artemis.native_app.core.ui.LinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalLinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.collectArtemisContextAsState
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.LinkBottomSheet
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.LinkBottomSheetState
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.PdfFile
import io.noties.markwon.LinkResolver

class MarkdownLinkResolverImpl(): MarkdownLinkResolver {
    @Composable
    override fun rememberMarkdownLinkResolver(): LinkResolver {
        val artemisContext by LocalArtemisContextProvider.current.collectArtemisContextAsState()
        val context = LocalContext.current
        val localLinkOpener = LocalLinkOpener.current

        val (bottomSheetState, setBottomSheetState) = remember(artemisContext) { mutableStateOf<LinkBottomSheetState?>(null) }

        if (bottomSheetState != null) {
            LinkBottomSheet(
                modifier = Modifier.fillMaxSize(),
                state = bottomSheetState,
                onDismissRequest = { setBottomSheetState(null) }
            )
        }

        return remember(context, localLinkOpener, artemisContext, setBottomSheetState) {
            BaseMarkdownLinkResolver(artemisContext, localLinkOpener, setBottomSheetState)
        }
    }
}

class BaseMarkdownLinkResolver(
    private val artemisContext: ArtemisContext,
    private val linkOpener: LinkOpener,
    private val setBottomSheetState: (LinkBottomSheetState) -> Unit
) : LinkResolver {
    override fun resolve(view: View, link: String) {
        // This workaround ensures other click functions are not triggered and prevents
        // the thread view from being opened when clicking on a link in the chat
        view.cancelPendingInputEvents()
        view.isPressed = false

        when {
            mediaHttpUri(link)?.path?.endsWith(".pdf", ignoreCase = true) == true -> {
                val token = if (sameMediaOrigin(artemisContext.serverUrl, link)) artemisContext.authTokenOrEmptyString else ""
                val account = (artemisContext as? ArtemisContext.LoggedIn)?.loginName.orEmpty()
                val key = mediaProgressKey(account, artemisContext.serverUrl, "attachment", link)
                val pdfFile = PdfFile(link, token, mediaHttpUri(link)?.path?.substringAfterLast('/').orEmpty(), artemisContext.serverUrl, key)
                setBottomSheetState(LinkBottomSheetState.PDFVIEWSTATE(pdfFile, key))
            }
            // TODO: open Artemis link in a Modal Bottom Sheet webview to attach session cookie (https://github.com/ls1intum/artemis-android/issues/245)
//            link.startsWith(serverUrl) -> {
//                setBottomSheetState(LinkBottomSheetState.WEBVIEWSTATE(link))
//            }
            else -> {
                linkOpener.openLink(link)
            }
        }
    }
}

