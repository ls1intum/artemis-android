package de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data

data class Faq(
    val id: Long,
    val questionTitle: String,
    val questionAnswer: String,
    val categories: List<FaqCategory>,
    val faqState: FaqState,
)