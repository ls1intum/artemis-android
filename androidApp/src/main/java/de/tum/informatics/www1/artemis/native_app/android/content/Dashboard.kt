package de.tum.informatics.www1.artemis.native_app.android.content

import kotlinx.serialization.Serializable

/**
 * A dashboard is a collection of courses.
 */
@Serializable
data class Dashboard(val courses: List<Course>)