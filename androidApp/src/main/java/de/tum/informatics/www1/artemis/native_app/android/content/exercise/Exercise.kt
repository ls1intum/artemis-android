package de.tum.informatics.www1.artemis.native_app.android.content.exercise

import android.os.Parcelable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.math.max
import kotlin.math.roundToInt

@JsonClassDiscriminator("type") //Default is type anyway, however here I make it explicit
@Serializable
sealed class Exercise : Parcelable {
    abstract val id: Int
    abstract val title: String
    abstract val maxPoints: Float
    abstract val bonusPoints: Float

    val maxPointsHalves: Int get() = (maxPoints * 2f).roundToInt()

    private val currentScore: Float
        get() = max(0f, maxPoints - 2f)

    val currentScoreHalves: Int get() = (currentScore * 2f).roundToInt()
}