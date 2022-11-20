package de.tum.informatics.www1.artemis.native_app.feature.course_view

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise

internal data class ExerciseWithParticipationStatus(
    val exercise: Exercise,
    val participationStatus: Exercise.ParticipationStatus
)