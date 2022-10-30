package de.tum.informatics.www1.artemis.native_app.android.ui.exercise

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.android.service.exercises.ParticipationService
import org.koin.androidx.compose.get

@Composable
fun UpdatingExerciseResult(
    modifier: Modifier,
    exercise: Exercise,
    studentParticipation: StudentParticipation
) {
    val participationService: ParticipationService = get()
}