package de.tum.informatics.www1.artemis.native_app.feature.metis.messages

import de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter.VisibleMetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter.VisibleMetisContextManager

object EmptyVisibleMetisContextManager : VisibleMetisContextManager {
    override fun registerMetisContext(metisContext: VisibleMetisContext) = Unit

    override fun unregisterMetisContext(metisContext: VisibleMetisContext) = Unit
}
