package de.tum.informatics.www1.artemis.native_app.core.ui.remote_images

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Row
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.link_resolving.LocalMarkdownLinkResolver
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.link_resolving.MarkdownLinkResolver
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.link_resolving.ReviewMarkdownLinkResolver
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.link_resolving.openReviewBrowser
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.ui.R
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.ProvideMarkwon

/** Rich student work and Markdown share one captured-session, cache-free image boundary. */
@Composable
fun ProvideReviewImages(session: ArtemisContext.LoggedIn, onDenied: () -> Unit, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val denied by rememberUpdatedState(onDenied)
    val provider = remember(session.serverUrl, session.loginName, session.authToken) { ReviewImageProvider(context, session) { denied() } }
    DisposableEffect(provider) { onDispose { provider.close() } }
    val failed by provider.hasImageFailure.collectAsState()
    var pendingBrowserLink by remember(session) { mutableStateOf<String?>(null) }
    var unsupportedLink by remember(session) { mutableStateOf(false) }
    val linkResolver = remember(session) {
        ReviewMarkdownLinkResolver(session.serverUrl, { pendingBrowserLink = it }, { unsupportedLink = true })
    }
    val markdownLinks = remember(linkResolver) { object : MarkdownLinkResolver {
        @Composable override fun rememberMarkdownLinkResolver() = linkResolver
    } }
    CompositionLocalProvider(LocalArtemisImageProvider provides provider, LocalMarkdownLinkResolver provides markdownLinks) {
        ProvideMarkwon {
            Column(Modifier.fillMaxSize()) {
                pendingBrowserLink?.let { link ->
                    Column(Modifier.padding(12.dp)) {
                        Text(stringResource(R.string.review_link_browser_signin), style = MaterialTheme.typography.bodyMedium)
                        Row {
                            TextButton(onClick = { openReviewBrowser(context, link); pendingBrowserLink = null }) { Text(stringResource(R.string.review_link_open_browser)) }
                            TextButton(onClick = { pendingBrowserLink = null }) { Text(stringResource(R.string.review_link_cancel)) }
                        }
                    }
                }
                if (unsupportedLink) Text(stringResource(R.string.review_link_unsupported), Modifier.padding(12.dp))
                if (failed) Text(stringResource(R.string.review_image_unavailable), Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                Box(Modifier.weight(1f)) { content() }
            }
        }
    }
}
