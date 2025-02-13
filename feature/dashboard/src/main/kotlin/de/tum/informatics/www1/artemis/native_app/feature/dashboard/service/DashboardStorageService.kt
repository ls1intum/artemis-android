package de.tum.informatics.www1.artemis.native_app.feature.dashboard.service

import kotlinx.coroutines.flow.Flow

interface DashboardStorageService {
    suspend fun onCourseAccessed(
        serverHost: String,
        courseId: Long
    )

    suspend fun getLastAccesssedCourses(
        serverHost: String,
    ): Flow<Map<Long, Long>>
}