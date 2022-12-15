package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.participate.text_exercise

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TextExerciseParticipationViewModel(initialText: String) : ViewModel() {

    private val _text = MutableStateFlow(initialText)
    val text: StateFlow<String> = _text

    fun updateText(newText: String) {
        _text.value = newText
    }
}