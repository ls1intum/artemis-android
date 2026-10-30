package de.tum.informatics.www1.artemis.native_app.core.ui.markdown.link_resolving

import android.content.Context
import android.content.Intent
import android.view.View
import android.provider.Browser
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import io.noties.markwon.LinkResolver
import java.net.URI

/** Review links never enter the global token-bearing PDF reader or cookie-enabled WebView. */
class ReviewMarkdownLinkResolver(
    serverUrl: String,
    private val onProtectedBrowserHandoff: (String) -> Unit,
    private val onUnsupported: () -> Unit,
) : LinkResolver {
    private val origin = URI(serverUrl.trimEnd('/') + "/")
    override fun resolve(view: View, link: String) {
        view.cancelPendingInputEvents()
        view.isPressed = false
        val destination = runCatching { origin.resolve(URI(link.replace(" ", "%20"))) }.getOrNull()
        if (destination == null || destination.scheme !in listOf("http", "https") || destination.userInfo != null) {
            onUnsupported()
            return
        }
        val sameOrigin = destination.scheme == origin.scheme && destination.host.equals(origin.host, ignoreCase = true) && port(destination) == port(origin)
        if (sameOrigin) onProtectedBrowserHandoff(destination.toASCIIString())
        else openReviewBrowser(view.context, destination.toASCIIString())
    }
    private fun port(uri: URI) = if (uri.port >= 0) uri.port else if (uri.scheme == "https") 443 else 80
}

/** Browser authentication is independent; no app Cookie, token or native PDF download is attached. */
fun openReviewBrowser(context: Context, url: String) {
    CustomTabsIntent.Builder().setShowTitle(true).build().apply {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.removeExtra(Browser.EXTRA_HEADERS)
    }.launchUrl(context, url.toUri())
}
