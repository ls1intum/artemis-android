package de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.impl

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.DashboardStorageService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

// Inspired by course-accessed-storage-service.ts on the Artemis repository
// https://github.com/ls1intum/Artemis/blob/b015d33d3e2badb7abfa3462c9ef1beb2795b391/src/main/webapp/app/course/course-access-storage.service.ts#L29
class DashboardStorageServiceImpl(private val context: Context) : DashboardStorageService {

    private companion object {
        private const val DATA_STORE_NAME = "dashboard_local_storage"

        private const val KEY_LAST_ACCESSED_COURSES = "last_accessed_courses"
        private const val MAX_DISPLAYED_RECENTLY_ACCESSED_COURSES = 3
    }

    private val Context.dataStore by preferencesDataStore(DATA_STORE_NAME)

    override suspend fun onCourseAccessed(
        serverHost: String,
        courseId: Long
    ) {
        context.dataStore.edit { data ->
            val lastAccessedCourses =
                data[getDashboardKey(serverHost, KEY_LAST_ACCESSED_COURSES)]

            val decodedLastAccessedCourses: MutableMap<Long, Long> =
                Json.decodeFromString(lastAccessedCourses ?: "{}")
            decodedLastAccessedCourses[courseId] = Clock.System.now().toEpochMilliseconds()

            if (decodedLastAccessedCourses.size > MAX_DISPLAYED_RECENTLY_ACCESSED_COURSES) {
                val oldestEntry = decodedLastAccessedCourses.minByOrNull { it.value }
                oldestEntry?.let { decodedLastAccessedCourses.remove(it.key) }
            }

            data[getDashboardKey(serverHost, KEY_LAST_ACCESSED_COURSES)] =
                Json.encodeToString(decodedLastAccessedCourses)
        }
    }

    override suspend fun getLastAccesssedCourses(
        serverHost: String,
    ): Flow<Map<Long, Long>> {
        return context.dataStore.data.map { preferences ->
            val storedJson = preferences[getDashboardKey(
                serverHost,
                KEY_LAST_ACCESSED_COURSES
            )] ?: "{}"
            Json.decodeFromString(storedJson)
        }
    }

    private fun getDashboardKey(serverHost: String, key: String): Preferences.Key<String> =
        stringPreferencesKey("$serverHost|$key")
}