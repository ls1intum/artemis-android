package de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission

import kotlinx.serialization.Serializable

@Serializable
data class BuildLogEntry(val log: String, val type: Type) {
    enum class Type {
        ERROR,
        WARNING,
        OTHER
    }
}