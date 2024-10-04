package de.tum.informatics.www1.artemis.native_app.core.model.exercise

import de.tum.informatics.www1.artemis.native_app.core.common.hasPassedFlow
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.QuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Attachment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("quiz")
data class QuizExercise(
    override val id: Long = 0L,
    override val title: String? = null,
    override val shortName: String? = null,
    override val maxPoints: Float? = null,
    override val bonusPoints: Float? = null,
    override val releaseDate: Instant? = null,
    override val dueDate: Instant? = null,
    override val assessmentDueDate: Instant? = null,
    override val difficulty: Difficulty? = null,
    override val mode: Mode = Mode.INDIVIDUAL,
    override val categories: List<Category> = emptyList(),
    override val visibleToStudents: Boolean? = null,
    override val teamMode: Boolean = false,
    override val studentAssignedTeamId: Long? = null,
    override val secondCorrectionEnabled: Boolean = false,
    override val presentationScoreEnabled: Boolean = false,
    override val studentAssignedTeamIdComputed: Boolean = false,
    override val problemStatement: String? = null,
    override val assessmentType: AssessmentType? = null,
    override val allowComplaintsForAutomaticAssessments: Boolean? = null,
    override val allowFeedbackRequests: Boolean? = null,
    override val includedInOverallScore: IncludedInOverallScore = IncludedInOverallScore.INCLUDED_COMPLETELY,
    override val exampleSolutionPublicationDate: Instant? = null,
    override val attachments: List<Attachment> = emptyList(),
    override val studentParticipations: List<Participation>? = null,
    override val course: Course? = null,
    val allowedNumberOfAttempts: Int? = null,
    val remainingNumberOfAttempts: Int? = null,
    val randomizeQuestionOrder: Boolean? = null,
    val isOpenForPractice: Boolean? = null,
    val duration: Int? = null,
    val quizQuestions: List<QuizQuestion> = emptyList(),
    val quizMode: QuizMode = QuizMode.INDIVIDUAL,
    val quizBatches: List<QuizBatch>? = null
) : Exercise() {

    override fun copyWithUpdatedParticipations(newParticipations: List<Participation>): Exercise =
        copy(studentParticipations = newParticipations)

//    fun participationStatusForQuizExercise(): ParticipationStatus {
//        if (exercise.status == QuizExercise.QuizStatus.CLOSED) {
//            if (exercise.studentParticipations?.isNotEmpty() == true && exercise.studentParticipations.first().results?.isNotEmpty() == true) {
//                return ParticipationStatus.QuizFinished(exercise.studentParticipations.first())
//            }
//            return ParticipationStatus.QuizNotParticipated
//        } else if (exercise.studentParticipations?.isNotEmpty() == true) {
//            val initState = exercise.studentParticipations.first().initializationState
//            if (initState == Participation.InitializationState.INITIALIZED) {
//                return ParticipationStatus.QuizActive
//            } else if (initState == Participation.InitializationState.FINISHED) {
//                return ParticipationStatus.QuizSubmitted
//            }
//        } else if (exercise.quizBatches?.any { it.started == true } == true) {
//            return ParticipationStatus.QuizNotInitialized
//        }
//        return ParticipationStatus.QuizNotStarted
//    }

    enum class QuizStatus {
        CLOSED,
        OPEN_FOR_PRACTICE,
        ACTIVE,
        VISIBLE,
        INVISIBLE,
    }

    enum class QuizMode {
        SYNCHRONIZED,
        BATCHED,
        INDIVIDUAL,
    }

    @Serializable
    data class QuizBatch(
        val id: Int? = null,
        val startTime: Instant? = null,
        val started: Boolean? = null,
        val ended: Boolean? = null,
        val submissionAllowed: Boolean? = null,
        val password: String? = null
    )
}

// Extensions

private val QuizExercise.statedQuizBatch: Boolean
    get() = quizBatches.orEmpty().any { it.started == true }

val QuizExercise.isUninitialized: Flow<Boolean>
    get() = notEndedSubmittedOrFinished.map { it && statedQuizBatch }

val QuizExercise.notStarted: Flow<Boolean>
    get() = notEndedSubmittedOrFinished.map { it && !statedQuizBatch }

val QuizExercise.quizEnded: Flow<Boolean>
    get() = dueDate?.hasPassedFlow() ?: flowOf(false)

val QuizExercise.quizStarted: Flow<Boolean>
    get() = releaseDate?.hasPassedFlow() ?: flowOf(true)

val QuizExercise.quizStatus: Flow<QuizExercise.QuizStatus>
    get() = combine(quizStarted, quizEnded) { quizStarted, quizEnded ->
        if (!quizStarted) return@combine QuizExercise.QuizStatus.INVISIBLE
        if (quizEnded) return@combine if (isOpenForPractice == true)
            QuizExercise.QuizStatus.OPEN_FOR_PRACTICE
        else QuizExercise.QuizStatus.CLOSED

        if (quizBatches.orEmpty()
                .any { it.started == true }
        ) return@combine QuizExercise.QuizStatus.ACTIVE
        QuizExercise.QuizStatus.VISIBLE
    }