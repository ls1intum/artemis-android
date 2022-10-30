package de.tum.informatics.www1.artemis.native_app.android.content.exercise.participation

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val participationSerializerModule = SerializersModule {
    polymorphic(Participation::class) {
        subclass(ProgrammingExerciseStudentParticipation::class)
        subclass(SolutionProgrammingExerciseParticipation::class)
        subclass(TemplateProgrammingExerciseParticipation::class)
        subclass(StudentParticipation.StudentParticipationImpl::class)
        defaultDeserializer { UnknownParticipation.serializer() }
    }
}