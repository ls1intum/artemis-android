package de.tum.informatics.www1.artemis.native_app.core.model.exercise

import de.tum.informatics.www1.artemis.native_app.core.common.hasPassedFlow
import de.tum.informatics.www1.artemis.native_app.core.common.isInFutureFlow
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation.InitializationState
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Attachment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.roundToInt

@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("type") //Default is type anyway, however here I make it explicit
@Serializable
sealed class Exercise {
    abstract val id: Long
    abstract val title: String?
    abstract val shortName: String?
    abstract val maxPoints: Float?
    abstract val bonusPoints: Float?
    abstract val releaseDate: Instant?
    abstract val dueDate: Instant?
    abstract val assessmentDueDate: Instant?
    abstract val difficulty: Difficulty?
    abstract val mode: Mode
    abstract val categories: List<Category>
    abstract val visibleToStudents: Boolean?
    abstract val teamMode: Boolean
    abstract val studentAssignedTeamId: Long?
    abstract val studentAssignedTeamIdComputed: Boolean
    abstract val problemStatement: String?
    abstract val assessmentType: AssessmentType?
    abstract val allowComplaintsForAutomaticAssessments: Boolean?
    abstract val allowManualFeedbackRequests: Boolean?
    abstract val includedInOverallScore: IncludedInOverallScore
    abstract val exampleSolutionPublicationDate: Instant?
    abstract val studentParticipations: List<Participation>?
    abstract val course: Course?

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

    enum class AssessmentType {
        AUTOMATIC,
        SEMI_AUTOMATIC,
        MANUAL
    }

    @Serializable(with = Category.CategorySerializer::class)
    data class Category(
        val category: String,
        val colorValue: Long?
    ) {
        //Categories are Json Objects wrapped in a string
        object CategorySerializer : KSerializer<Category> {
            override val descriptor: SerialDescriptor =
                PrimitiveSerialDescriptor("category", PrimitiveKind.STRING)

            override fun serialize(encoder: Encoder, value: Category) {
                throw NotImplementedError()
            }

            override fun deserialize(decoder: Decoder): Category {
                val categoryString = decoder.decodeString()

                return when(val parsedCategory = Json.parseToJsonElement(categoryString)) {
                    is JsonObject -> {
                        val category = parsedCategory["category"]?.jsonPrimitive?.content ?: ""
                        val colorString = parsedCategory["color"]?.jsonPrimitive?.content ?: ""

                        Category(category, parseColor(colorString))
                    }
                    else -> Category("", null)
                }
            }

            private fun parseColor(colorString: String): Long? {
                if (colorString == "null") return null
                if (!colorString.startsWith("#")) return null

                val code = colorString.substring(1).toLongOrNull(16) ?: return null
                return 0xff000000 + code
            }
        }
    }

    fun withUpdatedParticipation(participation: Participation): Exercise {
        //Replace the updated participation
        val updatedParticipations = studentParticipations?.map { oldParticipation ->
            if (oldParticipation.id == participation.id) {
                participation
            } else {
                oldParticipation
            }
        }
            ?: //The new participations are just the one we just received
            listOf(participation)

        return copyWithUpdatedParticipations(updatedParticipations)
    }

    /**
     * Create a copy of this exercise with the participations field replaced.
     */
    abstract fun copyWithUpdatedParticipations(newParticipations: List<Participation>): Exercise

    fun getDueDate(participation: Participation?): Instant? =
        if (dueDate == null) null else {
            participation?.individualDueDate ?: dueDate
        }
}

// Extensions

val Exercise.hasEnded: Flow<Boolean> get() = dueDate?.hasPassedFlow() ?: flowOf(false)

private val Exercise.notSubmittedOrFinished: Boolean
    get() = studentParticipations.orEmpty().firstOrNull()?.initializationDate == null ||
            studentParticipations!!.first().initializationState !in
            arrayOf(
                InitializationState.INITIALIZED,
                InitializationState.FINISHED
            )

val Exercise.notEndedSubmittedOrFinished: Flow<Boolean>
    get() = hasEnded.map { hasEnded ->
        val latestParticipation = latestParticipation

        !hasEnded && (
                latestParticipation?.initializationState == null ||
                        !(latestParticipation.initializationState == InitializationState.INITIALIZED
                                || latestParticipation.initializationState == InitializationState.FINISHED)
                )
    }

val Exercise.isStartExerciseAvailable: Flow<Boolean>
    get() {
        return if (this !is ProgrammingExercise) flowOf(true)
        else dueDate?.isInFutureFlow() ?: flowOf(true)
    }

private val Exercise.currentUserScore: Float?
    get() = studentParticipations
        .orEmpty()
        .firstOrNull()?.results?.maxBy { it.completionDate ?: Instant.fromEpochSeconds(0L) }
        ?.score

val Exercise.currentUserPoints: Float?
    get() {
        val maxPoints = maxPoints ?: return null
        val currentUserScore = currentUserScore ?: return null
        return maxPoints * (currentUserScore / 100f)
    }

val Exercise.latestParticipation: Participation?
    get() = studentParticipations.orEmpty().firstOrNull()