package de.tum.informatics.www1.artemis.native_app.feature.faq.service.remote

data class FaqDto(
    val id: Long,
    val questionTitle: String,
    val questionAnswer: String,
    val categories: List<String>,
    val faqState: String,
)