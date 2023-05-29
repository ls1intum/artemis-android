package de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto

interface IAnswerPost : IBasePost {
    val serverPostId: Long
    val resolvesPost: Boolean
}