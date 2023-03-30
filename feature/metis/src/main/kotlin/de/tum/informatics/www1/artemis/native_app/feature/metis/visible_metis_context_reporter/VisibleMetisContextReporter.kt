package de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter

import kotlinx.coroutines.flow.StateFlow

/**
 * Extension for activities that can report which metis contexts are currently displayed to the user.
 */
interface VisibleMetisContextReporter {
    val visibleMetisContexts: StateFlow<List<VisibleMetisContext>>
}