package de.tum.informatics.www1.artemis.native_app.core.model

import kotlinx.serialization.Serializable

/**
 * A dashboard is a collection of courses.
 */
@Serializable
data class Dashboard(
    val recentCourses: MutableList<CourseWithScore> = mutableListOf(),
    val courses: MutableList<CourseWithScore> = mutableListOf()
)
