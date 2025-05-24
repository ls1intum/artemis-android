package de.tum.informatics.www1.artemis.native_app.core.model.lecture

import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnit
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitAttachmentVideo
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitExercise
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitOnline
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitText
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitUnknown
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@OptIn(ExperimentalSerializationApi::class)
val lectureSerializerModule = SerializersModule {
    polymorphic(LectureUnit::class) {
        subclass(LectureUnitAttachmentVideo::class)
        subclass(LectureUnitExercise::class)
        subclass(LectureUnitText::class)
        subclass(LectureUnitOnline::class)

        defaultDeserializer { LectureUnitUnknown.serializer() }
    }
}