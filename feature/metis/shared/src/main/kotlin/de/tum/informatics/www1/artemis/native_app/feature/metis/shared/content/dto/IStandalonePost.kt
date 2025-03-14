package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto

import androidx.compose.runtime.Stable
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId

@Stable
interface IStandalonePost : IBasePost {
    val title: String?
    val answers: List<IAnswerPost>?
    val tags: List<String>?
    val resolved: Boolean?
    val displayPriority: DisplayPriority?

    val standalonePostId: StandalonePostId?

    val orderedAnswerPostings: List<IAnswerPost>
        get() = answers?.sortedBy { it.creationDate } ?: emptyList()
}