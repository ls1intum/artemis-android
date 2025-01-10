package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId

interface IStandalonePost : IBasePost {
    val title: String?
    val answers: List<IAnswerPost>?
    val tags: List<String>?
    val resolved: Boolean?
    val displayPriority: DisplayPriority?

    /**
     * A unique key which can be used to reference this post uniquely
     */
    val key: Any

    val standalonePostId: StandalonePostId?

    val orderedAnswerPostings: List<IAnswerPost>
        get() = answers?.sortedBy { it.creationDate } ?: emptyList()
}