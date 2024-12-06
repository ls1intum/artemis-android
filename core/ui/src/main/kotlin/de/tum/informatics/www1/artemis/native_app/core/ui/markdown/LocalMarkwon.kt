package de.tum.informatics.www1.artemis.native_app.core.ui.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.LocalArtemisImageProvider
import io.noties.markwon.Markwon

val LocalMarkwon: ProvidableCompositionLocal<Markwon?> =
    compositionLocalOf { null }

@Composable
fun ProvideMarkwon(content: @Composable () -> Unit) {
    val imageLoader = LocalArtemisImageProvider.current.rememberArtemisImageLoader()
    val context = LocalContext.current

    val imageWith = context.resources.displayMetrics.widthPixels
    val markdownRender: Markwon = remember(imageLoader) {
        createMarkdownRender(context, imageLoader, imageWith)
    }

    CompositionLocalProvider(LocalMarkwon provides markdownRender) {
        content()
    }
}