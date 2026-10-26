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

        return remember(context, localLinkOpener, artemisContext) {
            BaseMarkdownLinkResolver(artemisContext, localLinkOpener)
        }
    }
}

class BaseMarkdownLinkResolver(
    private val artemisContext: ArtemisContext,
    private val linkOpener: LinkOpener
) : LinkResolver {
    override fun resolve(view: View, link: String) {
        // This workaround ensures other click functions are not triggered and prevents
        // the thread view from being opened when clicking on a link in the chat
        view.cancelPendingInputEvents()
        view.isPressed = false

        when {
            mediaHttpUri(link)?.path?.endsWith(".pdf", ignoreCase = true) == true -> {
                linkOpener.openMediaPage(MediaPageRequest(
                    link, mediaHttpUri(link)?.path?.substringAfterLast('/').orEmpty(), "attachment",
                    mediaPageScope(artemisContext), MediaPageKind.PDF
                ))
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

