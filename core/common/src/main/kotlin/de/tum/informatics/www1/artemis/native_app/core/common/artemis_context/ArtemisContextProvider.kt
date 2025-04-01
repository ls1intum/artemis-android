package de.tum.informatics.www1.artemis.native_app.core.common.artemis_context

import kotlinx.coroutines.flow.StateFlow

interface ArtemisContextProvider {

    val stateFlow: StateFlow<ArtemisContext>

    fun setCourseId(courseId: Long)
    fun resetCourseId()
}