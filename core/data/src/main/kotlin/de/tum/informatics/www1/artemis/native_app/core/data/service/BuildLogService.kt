package de.tum.informatics.www1.artemis.native_app.core.data.service

import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.BuildLogEntry
import kotlinx.coroutines.flow.Flow

interface BuildLogService {

    /**
     * Retrieves the build logs for a given participation and optionally, a given result.
     * @param participationId The identifier of the participation.
     * @param resultId The identifier of an optional result to specify which submission to use
     */
    fun loadBuildLogs(participationId: Int, resultId: Int?, serverUrl: String, bearerToken: String): Flow<DataState<List<BuildLogEntry>>>
}