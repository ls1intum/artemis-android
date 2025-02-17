package de.tum.informatics.www1.artemis.native_app.core.data.service

import kotlinx.coroutines.flow.Flow

interface ArtemisContextBasedService {
    val onReloadRequired: Flow<Unit>
}