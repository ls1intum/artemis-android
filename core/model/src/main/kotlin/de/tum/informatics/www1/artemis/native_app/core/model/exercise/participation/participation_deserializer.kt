package de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@OptIn(ExperimentalSerializationApi::class)
val participationSerializerModule = SerializersModule {
    polymorphic(Participation::class) {
        subclass(ProgrammingExerciseStudentParticipation::class)
        subclass(SolutionProgrammingExerciseParticipation::class)
        subclass(TemplateProgrammingExerciseParticipation::class)
        subclass(StudentParticipation.StudentParticipationImpl::class)
        defaultDeserializer { UnknownParticipation.serializer() }
    }
}