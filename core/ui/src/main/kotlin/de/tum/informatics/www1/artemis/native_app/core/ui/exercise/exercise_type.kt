package de.tum.informatics.www1.artemis.native_app.core.ui.exercise

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.ui.graphics.vector.ImageVector
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.*

fun getExerciseTypeIcon(exercise: Exercise): ImageVector {
    return when (exercise) {
        is TextExercise -> Icons.Default.EditNote
        is ModelingExercise -> Icons.Outlined.AccountTree
        is FileUploadExercise -> Icons.Default.FileUpload
        is ProgrammingExercise -> Icons.Outlined.Keyboard
        is QuizExercise -> Icons.Default.Quiz
        else -> Icons.Default.QuestionMark
    }
}
