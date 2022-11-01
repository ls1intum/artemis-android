package de.tum.informatics.www1.artemis.native_app.android.ui.courses.course

import de.tum.informatics.www1.artemis.native_app.android.content.exercise.Exercise

data class ExerciseWithParticipationStatus(
    val exercise: Exercise,
    val participationStatus: Exercise.ParticipationStatus
)