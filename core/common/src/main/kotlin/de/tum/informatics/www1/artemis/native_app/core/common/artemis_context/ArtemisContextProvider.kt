package de.tum.informatics.www1.artemis.native_app.core.common.artemis_context

import kotlinx.coroutines.flow.Flow

interface ArtemisContextProvider {

    val current: Flow<ArtemisContext>
}