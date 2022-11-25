package de.tum.informatics.www1.artemis.native_app.core.model.metis

import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
class StandalonePost(
    override val id: Int? = null,
    override val author: User? = null,
    override val authorRole: UserRole? = null,
    override val creationDate: Instant? = null,
    override val content: String? = null,
    override val reactions: List<Reaction>? = null,
    val title: String? = null,
    val visibleForStudents: Boolean = true,
    val answers: List<AnswerPost>? = null,
    val tags: List<String>? = null,
    val exercise: Exercise? = null,
    val lecture: Lecture? = null,
    val course: Course? = null,
//    val plagiarismCase: PlagiarismCase? = null,
//    val conversation: Conversation? = null,
    val courseWideContext: CourseWideContext? = null,
    val displayPriority: DisplayPriority? = null,
    val resolved: Boolean? = null
) : BasePost()