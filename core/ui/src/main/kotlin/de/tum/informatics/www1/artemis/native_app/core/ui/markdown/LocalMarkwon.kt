package de.tum.informatics.www1.artemis.native_app.core.ui.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import io.noties.markwon.Markwon
import kotlinx.coroutines.flow.first

val LocalMarkwon: ProvidableCompositionLocal<Markwon?> =
    compositionLocalOf { null }

@Composable
fun ProvideMarkwon(imageLoader: ImageLoader? = null, content: @Composable () -> Unit) {
    val context = LocalContext.current

    val markdownRender: Markwon = remember(imageLoader) {
        createMarkdownRender(context, imageLoader)
    }

    CompositionLocalProvider(LocalMarkwon provides markdownRender) {
        content()
    }
}