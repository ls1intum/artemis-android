package de.tum.informatics.www1.artemis.native_app.core.datastore.room.course

import de.tum.informatics.www1.artemis.native_app.core.datastore.room.BaseDatabaseProvider

interface CourseDatabaseProvider : BaseDatabaseProvider {
    val courseDao: CourseDao
}