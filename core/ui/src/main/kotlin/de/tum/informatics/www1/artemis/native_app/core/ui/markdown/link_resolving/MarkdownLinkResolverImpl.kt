package de.tum.informatics.www1.artemis.native_app.core.ui.markdown.link_resolving

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
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

        return remember(context, authToken, serverUrl) {
            BaseMarkdownLinkResolver(context, authToken, serverUrl)
        }
    }
}

class BaseMarkdownLinkResolver(
    private val context: Context,
    private val authorizationToken: String,
    private val serverUrl: String = "",
) : LinkResolver {

    override fun resolve(view: View, link: String) {
        println(serverUrl)
        when {
            link.startsWith(serverUrl) -> {

            }

//            link.startsWith("customScheme://") -> {
//                // Handle custom scheme links
//                handleCustomScheme(link)
//            }

            else -> {
                Toast.makeText(context, "Unsupported link: $link", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleCustomScheme(link: String) {
        // Parse and handle custom scheme as needed
        Toast.makeText(context, "Handling custom scheme: $link", Toast.LENGTH_SHORT).show()
    }
}
