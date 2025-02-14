package de.tum.informatics.www1.artemis.native_app.feature.faq.service.remote

import kotlinx.serialization.Serializable

@Serializable
data class FaqDto(
    val id: Long,
    val questionTitle: String,
    val questionAnswer: String,
//    val categories: List<String>,     // TODO:  https://github.com/ls1intum/artemis-android/issues/399
    val faqState: String,
)