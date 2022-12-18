package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation

import androidx.lifecycle.ViewModel

class QuizParticipationViewModel(
    val exerciseId: Long,
    val quizType: QuizType
) : ViewModel() {
}