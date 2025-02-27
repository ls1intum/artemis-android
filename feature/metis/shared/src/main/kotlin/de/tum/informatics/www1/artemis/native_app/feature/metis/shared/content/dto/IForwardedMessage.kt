package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto

import androidx.compose.runtime.Stable

@Stable
sealed interface IForwardedMessage {
    val sourceId: Long?
    val sourceType: PostingType?
    val destinationPostId: Long?
    val destinationAnswerPostId: Long?
}