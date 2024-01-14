package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

sealed interface DataStatus {
    data object UpToDate : DataStatus

    data object Loading : DataStatus

    data object Outdated : DataStatus
}