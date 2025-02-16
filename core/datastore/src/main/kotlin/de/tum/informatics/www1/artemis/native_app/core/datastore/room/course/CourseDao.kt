package de.tum.informatics.www1.artemis.native_app.core.datastore.room.course

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CourseDao {

    @Insert
    suspend fun insert(course: CourseEntity): Long

    @Query("SELECT * FROM course WHERE server_url = :serverUrl AND id = :courseId")
    suspend fun getByServerUrlAndCourseId(serverUrl: String, courseId: Long): CourseEntity?


    suspend fun getOrCreateLocalId(serverUrl: String, courseId: Long): Long {
        val localId = getByServerUrlAndCourseId(serverUrl, courseId)?.localId ?:
            insert(CourseEntity(serverUrl = serverUrl, id = courseId))

        return localId
    }
}