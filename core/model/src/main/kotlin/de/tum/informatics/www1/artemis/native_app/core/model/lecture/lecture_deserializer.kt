package de.tum.informatics.www1.artemis.native_app.core.model.lecture

import de.tum.informatics.www1.artemis.native_app.core.model.lecture.attachment.Attachment
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.attachment.AttachmentFile
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.attachment.UnknownAttachment
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@OptIn(ExperimentalSerializationApi::class)
val lectureSerializerModule = SerializersModule {
    polymorphic(Attachment::class) {
        subclass(AttachmentFile::class)
        defaultDeserializer { UnknownAttachment.serializer() }
    }
}