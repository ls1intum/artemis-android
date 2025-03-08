package de.tum.informatics.www1.artemis.native_app.core.ui.compose

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState

// Adjusted from https://stackoverflow.com/a/69533584/13366254
enum class Keyboard {
    Opened, Closed
}

@Composable
fun keyboardAsState(): State<Keyboard> {
    val isImeVisible = WindowInsets.isImeVisible
    return rememberUpdatedState(if (isImeVisible) Keyboard.Opened else Keyboard.Closed)
}