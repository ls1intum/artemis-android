package de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("submissionExerciseType")
@Serializable
sealed class Submission {
    abstract val id: Int?
    abstract val submitted: Boolean?
    abstract val submissionDate: Instant?
    abstract val exampleSubmission: Boolean?
    abstract val durationInMinutes: Float?
    abstract val results: List<Result>?
    abstract val participation: Participation?
    abstract val submissionType: SubmissionType?
}

enum class SubmissionType {
    MANUAL,
    TIMEOUT,
    INSTRUCTOR,
    EXTERNAL,
    TEST,
    ILLEGAL
}