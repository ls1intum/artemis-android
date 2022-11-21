package de.tum.informatics.www1.artemis.native_app.core.model.exercise.feedback

import kotlinx.serialization.Serializable

@Serializable
data class StaticCodeAnalysisIssue(
    val rule: String = "",
    val message: String = "",
    val penalty: Float? = null
)