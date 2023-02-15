package de.tum.informatics.www1.artemis.native_app.core.model.metis

interface IStandalonePost : IBasePost {
    val serverPostId: Long
    val title: String?
    val answers: List<IAnswerPost>?
    val tags: List<String>?
    val resolved: Boolean?
}