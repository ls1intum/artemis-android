package de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
class UnknownSubmission(
    override val id: Int? = null,
    override val submitted: Boolean? = null,
    override val submissionDate: Instant? = null,
    override val exampleSubmission: Boolean? = null,
    override val durationInMinutes: Float? = null,
    override val results: List<Result>? = null,
    override val participation: Participation? = null,
) : Submission()