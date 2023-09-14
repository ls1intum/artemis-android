package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.TextSubmission

sealed interface SyncState {
    data class Synced(val submission: TextSubmission) : SyncState

    /**
     * The text is not up to date, but we are waiting before actually syncing.
     */
    object SyncPending : SyncState

    object Syncing : SyncState
    object SyncFailed : SyncState
}