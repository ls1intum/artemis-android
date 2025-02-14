package de.tum.informatics.www1.artemis.native_app.core.ui.markdown.link_resolving

import android.content.Context
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.common.markdown.MarkdownUrlUtil
import de.tum.informatics.www1.artemis.native_app.core.ui.LinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalLinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.collectAsState
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.LinkBottomSheet
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.LinkBottomSheetState
import io.noties.markwon.LinkResolver

class MarkdownLinkResolverImpl(
    private val artemisContextProvider: ArtemisContextProvider
): MarkdownLinkResolver {
    @Composable
    override fun rememberMarkdownLinkResolver(): LinkResolver {
        val artemisContext by artemisContextProvider.collectAsState()
        val serverUrl = artemisContext.serverUrl
        val authToken = artemisContext.authToken
        val context = LocalContext.current
        val localLinkOpener = LocalLinkOpener.current

        val (bottomSheetLink, setLinkToShow) = remember { mutableStateOf<String?>(null) }
        val (bottomSheetState, setBottomSheetState) = remember { mutableStateOf(LinkBottomSheetState.WEBVIEWSTATE) }

        if (bottomSheetLink != null) {
            val filename =
                if (bottomSheetState == LinkBottomSheetState.PDFVIEWSTATE) MarkdownUrlUtil.decodeUrl(
                    bottomSheetLink.substringAfterLast("/")
                ) else null
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

        return remember(context, localLinkOpener, authToken, serverUrl) {
            BaseMarkdownLinkResolver(context, localLinkOpener, serverUrl, setLinkToShow, setBottomSheetState)
        }
    }
}

class BaseMarkdownLinkResolver(
    private val context: Context,
    private val linkOpener: LinkOpener,
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
                linkOpener.openLink(link)
            }
        }
    }
}

