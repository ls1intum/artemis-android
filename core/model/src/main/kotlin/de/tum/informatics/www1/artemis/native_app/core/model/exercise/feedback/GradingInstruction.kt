package de.tum.informatics.www1.artemis.native_app.core.model.exercise.feedback

import kotlinx.serialization.Serializable

@Serializable
class GradingInstruction(
    val id: Int?,
    val credits: Float,
    val feedback: String
)