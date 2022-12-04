package de.tum.informatics.www1.artemis.native_app.core.model.metis

import kotlinx.serialization.Serializable

@Serializable
enum class CourseWideContext(val httpValue: String) {
    ANNOUNCEMENT("ANNOUNCEMENT"),
    TECH_SUPPORT("TECH_SUPPORT"),
    ORGANIZATION("ORGANIZATION"),
    RANDOM("RANDOM")
}