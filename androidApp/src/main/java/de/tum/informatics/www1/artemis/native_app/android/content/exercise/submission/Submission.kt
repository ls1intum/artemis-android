package de.tum.informatics.www1.artemis.native_app.android.content.exercise.submission

import de.tum.informatics.www1.artemis.native_app.android.content.exercise.participation.Participation
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@JsonClassDiscriminator("type")
@Serializable
sealed class Submission {

    abstract val id: Int?
    abstract val submitted: Boolean?
    abstract val submissionDate: Instant?
    abstract val exampleSubmission: Boolean?
    abstract val durationInMinutes: Float?
    abstract val results: List<Result>?
    abstract val participation: Participation?
}