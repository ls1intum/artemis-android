package de.tum.informatics.www1.artemis.native_app.android.content.lecture

import de.tum.informatics.www1.artemis.native_app.android.content.lecture.attachment.Attachment
import de.tum.informatics.www1.artemis.native_app.android.content.lecture.attachment.AttachmentFile
import de.tum.informatics.www1.artemis.native_app.android.content.lecture.attachment.UnknownAttachment
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val lectureSerializerModule = SerializersModule {
    polymorphic(Attachment::class) {
        subclass(AttachmentFile::class)
        defaultDeserializer { UnknownAttachment.serializer() }
    }
}