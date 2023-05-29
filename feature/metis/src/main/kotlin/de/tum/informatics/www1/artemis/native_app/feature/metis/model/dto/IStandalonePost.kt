package de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto

interface IStandalonePost : IBasePost {
    val serverPostId: Long
    val title: String?
    val answers: List<IAnswerPost>?
    val tags: List<String>?
    val resolved: Boolean?
}