package de.tum.informatics.www1.artemis.native_app.android.content.exercise

import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Default deserialized exercise if an exercise is found this app does not know.
 */
@Serializable
@Parcelize
class UnknownExercise(
    override val id: Int,
    override val title: String,
    override val maxPoints: Float,
    override val bonusPoints: Float
) : Exercise()