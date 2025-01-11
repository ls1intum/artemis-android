package de.tum.informatics.www1.artemis.native_app.feature.metistest

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisibleMetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisibleMetisContextManager

object VisibleMetisContextManagerMock : VisibleMetisContextManager {
    private val registeredMetisContexts = mutableListOf<VisibleMetisContext>()

    override fun registerMetisContext(metisContext: VisibleMetisContext) {
        registeredMetisContexts.add(metisContext)
    }

    override fun unregisterMetisContext(metisContext: VisibleMetisContext) {
        registeredMetisContexts.remove(metisContext)
    }

    override fun getRegisteredMetisContexts(): List<VisibleMetisContext> = registeredMetisContexts
}
