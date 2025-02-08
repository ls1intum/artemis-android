package de.tum.informatics.www1.artemis.native_app.feature.faq.service.remote

import kotlinx.serialization.Serializable

@Serializable
data class FaqDto(
    val id: Long,
    val questionTitle: String,
    val questionAnswer: String,
//    val categories: List<String>,
    val faqState: String,
)