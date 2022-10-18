package de.tum.informatics.www1.artemis.native_app.android.content.exercise

import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("text")
@Parcelize
class TextExercise(override val id: Int, override val title: String, override val maxPoints: Float,
                   override val bonusPoints: Float
) : Exercise()