package de.tum.informatics.www1.artemis.native_app.core.ui.exercise

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.FileUploadExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ModelingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ProgrammingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.TextExercise
import de.tum.informatics.www1.artemis.native_app.core.ui.R

@Composable
fun getExerciseTypeIconPainter(exercise: Exercise?): Painter? {
    return when (exercise) {
        is TextExercise -> painterResource(id = R.drawable.font)
        is ModelingExercise -> painterResource(id = R.drawable.diagram_project)
        is FileUploadExercise -> painterResource(id = R.drawable.file_arrow_up)
        is ProgrammingExercise -> painterResource(id = R.drawable.keyboard)
        is QuizExercise -> painterResource(id = R.drawable.check_double)
        null -> null
        else -> rememberVectorPainter(image = Icons.Default.QuestionMark)
    }
}
