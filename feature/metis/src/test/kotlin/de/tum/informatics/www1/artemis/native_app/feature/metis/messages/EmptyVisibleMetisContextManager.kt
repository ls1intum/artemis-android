package de.tum.informatics.www1.artemis.native_app.feature.metis.messages

import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.visiblemetiscontextreporter.VisibleMetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.visiblemetiscontextreporter.VisibleMetisContextManager

object EmptyVisibleMetisContextManager : VisibleMetisContextManager {
    override fun registerMetisContext(metisContext: VisibleMetisContext) = Unit

    override fun unregisterMetisContext(metisContext: VisibleMetisContext) = Unit
}
