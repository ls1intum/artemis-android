package de.tum.informatics.www1.artemis.native_app.android.content.exercise

import androidx.compose.ui.graphics.Color
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.participation.Participation.InitializationState
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.android.content.lecture.attachment.Attachment
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonClassDiscriminator
import org.json.JSONObject
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
    abstract val participationStatus: ParticipationStatus?
    abstract val problemStatement: String?
    abstract val assessmentType: AssessmentType?
    abstract val allowComplaintsForAutomaticAssessments: Boolean?
    abstract val allowManualFeedbackRequests: Boolean?
    abstract val includedInOverallScore: IncludedInOverallScore
    abstract val exampleSolutionPublicationDate: Instant?
    abstract val studentParticipations: List<Participation>?

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

    @Serializable(with = Category.CategorySerializer::class)
    data class Category(
        val category: String,
        val color: Color?
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

                val obj = JSONObject(categoryString)
                val category = obj.getString("category")
                val colorString = obj.getString("color")

                return Category(category, parseColor(colorString))
            }

            private fun parseColor(colorString: String): Color? {
                if (colorString == "null") return null
                if (!colorString.startsWith("#")) return null

                val code = colorString.substring(1).toLongOrNull(16) ?: return null
                return Color(0xff000000 + code)
            }
        }
    }

    /**
     * Create a copy of this exercise with the participations field replaced.
     */
    abstract fun copyWithUpdatedParticipations(newParticipations: List<Participation>): Exercise

    //-------------------------------------------------------------
    // Copy of https://github.com/ls1intum/Artemis/blob/5c13e2e1b5b6d81594b9123946f040cbf6f0cfc6/src/main/webapp/app/exercises/shared/exercise/exercise.utils.ts
    // TODO: Remove me once this is calculated on the server.

    fun computeParticipationStatus(testRun: Boolean?): ParticipationStatus {
        val studentParticipation = if (testRun == null) {
            studentParticipations.orEmpty().firstOrNull()
        } else {
            studentParticipations.orEmpty().firstOrNull {
                if (it is StudentParticipation) {
                    it.testRun == testRun
                } else false
            }
        }

        // For team exercises check whether the student has been assigned to a team yet
        // !!!! TODO: Not yet implemented
//        if (teamMode == true && studentAssignedTeamIdComputed && !studentAssignedTeamId) {
//            return ParticipationStatus.NO_TEAM_ASSIGNED
//        }

        // Evaluate the participation status for quiz exercises.
        if (this is QuizExercise) {
            return participationStatusForQuizExercise(this)
        }

        // Evaluate the participation status for modeling, text and file upload exercises if the exercise has participations.
        if ((this is ModelingExercise || this is TextExercise || this is FileUploadExercise) && studentParticipation != null) {
            return participationStatusForModelingTextFileUploadExercise(studentParticipation)
        }

        val initState = studentParticipation?.initializationState

        // The following evaluations are relevant for programming exercises in general and for modeling, text and file upload exercises that don't have participations.
        if (studentParticipation == null ||
            initState == InitializationState.UNINITIALIZED ||
            initState == InitializationState.REPO_COPIED ||
            initState == InitializationState.REPO_CONFIGURED ||
            initState == InitializationState.BUILD_PLAN_COPIED ||
            initState == InitializationState.BUILD_PLAN_CONFIGURED
        ) {
            if (this is ProgrammingExercise && !isStartExerciseAvailable(this) && testRun == null || testRun == false) {
                return ParticipationStatus.EXERCISE_MISSED
            } else {
                return ParticipationStatus.UNINITIALIZED
            }
        } else if (studentParticipation.initializationState === InitializationState.INITIALIZED) {
            return ParticipationStatus.INITIALIZED
        }
        return ParticipationStatus.INACTIVE
    }

    private fun isStartExerciseAvailable(exercise: ProgrammingExercise) =
        exercise.dueDate == null || Clock.System.now() < exercise.dueDate

    private fun participationStatusForQuizExercise(exercise: QuizExercise): ParticipationStatus {
        if (exercise.status == QuizExercise.QuizStatus.CLOSED) {
            if (exercise.studentParticipations?.isNotEmpty() == true && exercise.studentParticipations.first().results?.isNotEmpty() == true) {
                return ParticipationStatus.QUIZ_FINISHED
            }
            return ParticipationStatus.QUIZ_NOT_PARTICIPATED
        } else if (exercise.studentParticipations?.isNotEmpty() == true) {
            val initState = exercise.studentParticipations.first().initializationState
            if (initState == InitializationState.INITIALIZED) {
                return ParticipationStatus.QUIZ_ACTIVE
            } else if (initState == InitializationState.FINISHED) {
                return ParticipationStatus.QUIZ_SUBMITTED
            }
        } else if (exercise.quizBatches?.any { it.started == true } == true) {
            return ParticipationStatus.QUIZ_UNINITIALIZED
        }
        return ParticipationStatus.QUIZ_NOT_STARTED
    }

    private fun participationStatusForModelingTextFileUploadExercise(participation: Participation): ParticipationStatus {
        return if (participation.initializationState == InitializationState.INITIALIZED) {
            if (hasDueDataPassed(participation)) ParticipationStatus.EXERCISE_MISSED else ParticipationStatus.EXERCISE_ACTIVE
        } else if (participation.initializationState == InitializationState.FINISHED) ParticipationStatus.EXERCISE_SUBMITTED
        else ParticipationStatus.UNINITIALIZED
    }

    private fun hasDueDataPassed(participation: Participation): Boolean {
        return if (dueDate == null) false else {
            (getDueDate(participation) ?: return false) > Clock.System.now()
        }
    }

    fun getDueDate(participation: Participation): Instant? =
        if (dueDate == null) null else {
            participation.initializationDate ?: dueDate
        }
}