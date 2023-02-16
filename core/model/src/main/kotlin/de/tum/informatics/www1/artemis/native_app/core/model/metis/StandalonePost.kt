package de.tum.informatics.www1.artemis.native_app.core.model.metis

import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class StandalonePost(
    override val id: Long?,
    override val author: User? = null,
    override val authorRole: UserRole? = null,
    override val creationDate: Instant? = null,
    override val content: String? = null,
    override val reactions: List<Reaction>? = null,
    override val title: String? = null,
    val visibleForStudents: Boolean = true,
    override val answers: List<AnswerPost>? = null,
    override val tags: List<String>? = null,
    val exercise: Exercise? = null,
    val lecture: Lecture? = null,
    val course: Course? = null,
//    val plagiarismCase: PlagiarismCase? = null,
//    val conversation: Conversation? = null,
    val courseWideContext: CourseWideContext? = null,
    val displayPriority: DisplayPriority? = null,
    override val resolved: Boolean? = null
) : BasePost(), IStandalonePost {

    @Transient
    override val serverPostId: Long = id ?: 0L
}