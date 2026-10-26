package de.tum.informatics.www1.artemis.native_app.feature.exerciseview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier

/** Move the same exercise composition between adaptive slots, retaining its route and assessment. */
@Composable
internal fun rememberExerciseDetailPane(
    exerciseId: Long?,
    content: @Composable (Modifier) -> Unit,
): @Composable (Modifier) -> Unit {
    val currentContent by rememberUpdatedState(content)
    return remember(exerciseId) {
        movableContentOf { modifier: Modifier -> currentContent(modifier) }
    }
}
