package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf

val LocalVisibleMetisContextManager =
    compositionLocalOf<VisibleMetisContextManager> { throw IllegalStateException("No VisibleMetisContextManager provided.") }

interface VisibleMetisContextManager {
    fun registerMetisContext(metisContext: VisibleMetisContext)

    fun unregisterMetisContext(metisContext: VisibleMetisContext)

    fun getRegisteredMetisContexts(): List<VisibleMetisContext>

    fun getMostRecentMetisContext(): VisibleMetisContext? {
        return getRegisteredMetisContexts().lastOrNull()
    }
}

@Composable
fun ReportVisibleMetisContext(visibleMetisContext: VisibleMetisContext) {
    val visibleMetisContextManager = LocalVisibleMetisContextManager.current
    DisposableEffect(visibleMetisContextManager, visibleMetisContext) {
        visibleMetisContextManager.registerMetisContext(visibleMetisContext)

        onDispose {
            visibleMetisContextManager.unregisterMetisContext(visibleMetisContext)
        }
    }
}