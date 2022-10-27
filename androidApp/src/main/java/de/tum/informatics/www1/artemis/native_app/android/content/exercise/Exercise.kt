package de.tum.informatics.www1.artemis.native_app.android.content.exercise

import android.os.Parcelable
import android.provider.Telephony.Mms.Part
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import de.tum.informatics.www1.artemis.native_app.android.content.lecture.attachment.Attachment
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.math.max
import kotlin.math.roundToInt

@JsonClassDiscriminator("type") //Default is type anyway, however here I make it explicit
@Serializable
sealed class Exercise {
    abstract val id: Int?
    abstract val title: String?
    abstract val shortName: String?
    abstract val maxPoints: Float?
    abstract val bonusPoints: Float?
    abstract val releaseDate: Instant?
    abstract val dueDate: Instant?
    abstract val difficulty: Difficulty?
    abstract val mode: Mode
    abstract val categories: List<Category>
    abstract val visibleToStudents: Boolean?
    abstract val teamMode: Boolean?
    abstract val participationStatus: ParticipationStatus?
    abstract val problemStatement: String?
    abstract val assessmentType: AssessmentType?
    abstract val allowComplaintsForAutomaticAssessments: Boolean?
    abstract val allowManualFeedbackRequests: Boolean?
    abstract val includedInOverallScore: IncludedInOverallScore
    abstract val exampleSolutionPublicationDate: Instant?

    // -------
    abstract val attachments: List<Attachment>


    val maxPointsHalves: Int get() = 3

    private val currentScore: Float
        get() = 4f

    val currentScoreHalves: Int get() = (currentScore * 2f).roundToInt()

    enum class Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

    enum class Mode {
        INDIVIDUAL,
        TEAM
    }

    // IMPORTANT NOTICE: The following strings have to be consistent with the ones defined in Exercise.java
    enum class IncludedInOverallScore {
        INCLUDED_COMPLETELY,
        INCLUDED_AS_BONUS,
        NOT_INCLUDED
    }

    enum class ParticipationStatus {
        QUIZ_UNINITIALIZED,
        QUIZ_ACTIVE,
        QUIZ_SUBMITTED,
        QUIZ_NOT_STARTED,
        QUIZ_NOT_PARTICIPATED,
        QUIZ_FINISHED,
        NO_TEAM_ASSIGNED,
        UNINITIALIZED,
        INITIALIZED,
        INACTIVE,
        EXERCISE_ACTIVE,
        EXERCISE_SUBMITTED,
        EXERCISE_MISSED
    }

    enum class AssessmentType {
        AUTOMATIC,
        SEMI_AUTOMATIC,
        MANUAL
    }

    @Serializable
    data class Category(
        val category: String,
        @Serializable(with = CategoryColorSerializer::class) val color: Color?
    ) {
        object CategoryColorSerializer : KSerializer<Color?> {
            override val descriptor: SerialDescriptor =
                PrimitiveSerialDescriptor("color", PrimitiveKind.STRING)

            override fun serialize(encoder: Encoder, value: Color?) {
                if (value != null) {
                    val asString = value.toArgb().toString(16).padStart(8, '0')
                    encoder.encodeString("#$asString")
                } else encoder.encodeString("null")
            }

            override fun deserialize(decoder: Decoder): Color? {
                val s = decoder.decodeString()

                if (s == "null") return null
                if (!s.startsWith("#")) return null

                val code = s.substring(1).toLongOrNull(16) ?: return null
                return Color(code)
            }
        }
    }
}