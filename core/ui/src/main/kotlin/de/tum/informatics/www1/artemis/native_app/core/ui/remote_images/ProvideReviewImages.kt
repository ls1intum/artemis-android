package de.tum.informatics.www1.artemis.native_app.core.ui.remote_images

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
    CompositionLocalProvider(LocalArtemisImageProvider provides provider) {
        ProvideMarkwon {
            Column(Modifier.fillMaxSize()) {
                if (failed) Text(stringResource(R.string.review_image_unavailable), Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                Box(Modifier.weight(1f)) { content() }
            }
        }
    }
}
