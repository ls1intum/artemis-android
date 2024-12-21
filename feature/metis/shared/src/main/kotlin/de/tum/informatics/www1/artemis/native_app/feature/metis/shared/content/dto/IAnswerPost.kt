package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto

interface IAnswerPost : IBasePost {
    val resolvesPost: Boolean
    val parentAuthorId: Long?
}