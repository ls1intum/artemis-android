package de.tum.informatics.www1.artemis.native_app.core.model.lecture

import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@OptIn(ExperimentalSerializationApi::class)
val lectureSerializerModule = SerializersModule {
    polymorphic(LectureUnit::class) {
        subclass(LectureUnitAttachment::class)
        subclass(LectureUnitExercise::class)
        subclass(LectureUnitText::class)
        subclass(LectureUnitVideo::class)
        subclass(LectureUnitOnline::class)

        defaultDeserializer { LectureUnitUnknown.serializer() }
    }
}