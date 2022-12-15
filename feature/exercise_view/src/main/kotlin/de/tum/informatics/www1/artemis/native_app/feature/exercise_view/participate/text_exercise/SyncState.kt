package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.participate.text_exercise

sealed interface SyncState {
    object Synced : SyncState
    object Syncing : SyncState
    data class SyncFailed(val retry: () -> Unit) : SyncState
}