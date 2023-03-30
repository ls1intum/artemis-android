package de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter

import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext

sealed interface VisibleMetisContext {
    val metisContext: MetisContext
}

data class VisiblePostList(override val metisContext: MetisContext) : VisibleMetisContext

data class VisibleStandalonePostDetails(override val metisContext: MetisContext, val postId: Long) : VisibleMetisContext