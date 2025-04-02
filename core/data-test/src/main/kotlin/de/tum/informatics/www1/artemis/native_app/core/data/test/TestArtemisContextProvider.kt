package de.tum.informatics.www1.artemis.native_app.core.data.test

import de.tum.informatics.www1.artemis.native_app.core.data.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.data.artemis_context.ArtemisContextImpl
import de.tum.informatics.www1.artemis.native_app.core.data.artemis_context.ArtemisContextProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TestArtemisContextProvider(
    override val stateFlow: StateFlow<ArtemisContext> = MutableStateFlow(
        ArtemisContextImpl.Empty),
) : ArtemisContextProvider {

    constructor(artemisContext: ArtemisContext = ArtemisContextImpl.Empty) : this(
        MutableStateFlow(artemisContext)
    )

    override fun setCourseId(courseId: Long) {
        // No-op
    }

    override fun clearCourseId() {
        // No-op
    }

}