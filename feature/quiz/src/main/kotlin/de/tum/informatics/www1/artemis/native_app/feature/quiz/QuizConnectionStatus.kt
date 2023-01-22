package de.tum.informatics.www1.artemis.native_app.feature.quiz

sealed interface QuizConnectionStatus {
    /**
     * In practice mode there is no connection and therefore we have no connection state
     */
    object PracticeMode : QuizConnectionStatus

    data class LiveMode(val isConnected: Boolean) : QuizConnectionStatus
}