package de.tum.informatics.www1.artemis.native_app.core.ui.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.link_resolving.LocalMarkdownLinkResolver
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.LocalArtemisImageProvider
import io.noties.markwon.Markwon

val LocalMarkwon: ProvidableCompositionLocal<Markwon?> =
    compositionLocalOf { null }

@Composable
fun ProvideMarkwon(
    useOriginalImageSize: Boolean = false,
    content: @Composable () -> Unit
) {
    val imageLoader = LocalArtemisImageProvider.current.rememberArtemisImageLoader()
    val linkResolver = LocalMarkdownLinkResolver.current.rememberMarkdownLinkResolver()
    val context = LocalContext.current

    val imageWidth = context.resources.displayMetrics.widthPixels
    val markdownRender: Markwon = remember(imageLoader, linkResolver, useOriginalImageSize) {
        createMarkdownRender(context, imageLoader, linkResolver, imageWidth, useOriginalImageSize)
    }

    CompositionLocalProvider(LocalMarkwon provides markdownRender) {
        content()
    }
}