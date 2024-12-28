package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.BasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.model.SavedPostStatus

interface SavedPostService {

    suspend fun getAllSavedPosts(
        courseId: Long,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Map<SavedPostStatus, List<BasePost>>> {
        val savedPosts = mutableMapOf<SavedPostStatus, List<BasePost>>()
        SavedPostStatus.entries.forEach { status ->
            val result = getSavedPosts(status, courseId, authToken, serverUrl)
            if (result is NetworkResponse.Response) {
                savedPosts[status] = result.data
            } else if (result is NetworkResponse.Failure) {
                return NetworkResponse.Failure(result.exception)
            }
        }
        return NetworkResponse.Response(savedPosts)
    }

    suspend fun getSavedPosts(
        status: SavedPostStatus,
        courseId: Long,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<List<BasePost>>
}