package de.tum.informatics.www1.artemis.native_app.core.model.exercise

import de.tum.informatics.www1.artemis.native_app.core.common.hasPassedFlow
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation.InitializationState
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.attachment.Attachment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
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
    abstract val assessmentDueDate: Instant?
    abstract val difficulty: Difficulty?
    abstract val mode: Mode
    abstract val categories: List<Category>
    abstract val visibleToStudents: Boolean?
    abstract val teamMode: Boolean?
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

                val obj = Json.parseToJsonElement(categoryString).jsonObject
                val category = obj["category"]?.jsonPrimitive?.content ?: ""
                val colorString = obj["color"]?.jsonPrimitive?.content ?: ""

                return Category(category, parseColor(colorString))
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

    //-------------------------------------------------------------
    // Copy of https://github.com/ls1intum/Artemis/blob/5c13e2e1b5b6d81594b9123946f040cbf6f0cfc6/src/main/webapp/app/exercises/shared/exercise/exercise.utils.ts
    // TODO: Remove me once this is calculated on the server.

    private fun isStartExerciseAvailable(exercise: ProgrammingExercise) =
        exercise.dueDate == null || Clock.System.now() < exercise.dueDate

//    private fun participationStatusForModelingTextFileUploadExercise(participation: Participation): ParticipationStatus {
//        return if (participation.initializationState == InitializationState.INITIALIZED) {
//            if (hasDueDataPassed(participation)) ParticipationStatus.ExerciseMissed else ParticipationStatus.ExerciseActive
//        } else if (participation.initializationState == InitializationState.FINISHED) ParticipationStatus.ExerciseSubmitted(
//            participation
//        )
//        else ParticipationStatus.Uninitialized
//    }

    private fun hasDueDataPassed(participation: Participation): Boolean {
        return if (dueDate == null) false else {
            (getDueDate(participation) ?: return false) > Clock.System.now()
        }
    }

    fun getDueDate(participation: Participation?): Instant? =
        if (dueDate == null) null else {
            participation?.initializationDate ?: dueDate
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
        !hasEnded && notSubmittedOrFinished
    }