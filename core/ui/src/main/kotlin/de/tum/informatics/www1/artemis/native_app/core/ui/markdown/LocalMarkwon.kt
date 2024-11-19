package de.tum.informatics.www1.artemis.native_app.core.ui.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import io.noties.markwon.Markwon

val LocalMarkwon: ProvidableCompositionLocal<Markwon?> =
    compositionLocalOf { null }

@Composable
fun ProvideMarkwon(imageLoader: ImageLoader? = null, content: @Composable () -> Unit) {
    val context = LocalContext.current

    val imageWith = context.resources.displayMetrics.widthPixels
    val markdownRender: Markwon = remember(imageLoader) {
        println(imageWith)
        createMarkdownRender(context, imageLoader, imageWith)
    }

    CompositionLocalProvider(LocalMarkwon provides markdownRender) {
        content()
    }
}