package de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf

private val localVisibleMetisContextManager =
    compositionLocalOf<VisibleMetisContextManager> { throw IllegalStateException("No VisibleMetisContextManager provided.") }

interface VisibleMetisContextManager {
    fun registerMetisContext(metisContext: VisibleMetisContext)

    fun unregisterMetisContext(metisContext: VisibleMetisContext)
}

@Composable
fun ProvideLocalVisibleMetisContextManager(
    visibleMetisContextManager: VisibleMetisContextManager,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        localVisibleMetisContextManager provides visibleMetisContextManager,
        content = content
    )
}

@Composable
internal fun ReportVisibleMetisContext(visibleMetisContext: VisibleMetisContext) {
    val visibleMetisContextManager = localVisibleMetisContextManager.current
    DisposableEffect(visibleMetisContextManager, visibleMetisContext) {
        visibleMetisContextManager.registerMetisContext(visibleMetisContext)

        onDispose {
            visibleMetisContextManager.unregisterMetisContext(visibleMetisContext)
        }
    }
}