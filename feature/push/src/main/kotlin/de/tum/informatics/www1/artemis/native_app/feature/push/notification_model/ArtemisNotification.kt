package de.tum.informatics.www1.artemis.native_app.feature.push.notification_model

import de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model.CommunicationType
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.NotificationTargetManager
import kotlinx.datetime.Instant
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

@Serializable(with = ArtemisNotificationDeserializer::class)
sealed interface ArtemisNotification<T : NotificationType> {
    val type: T
    val notificationPlaceholders: List<String>
    val target: String
    val date: Instant
}

@Serializable
data class MiscArtemisNotification(
    override val type: MiscNotificationType,
    override val notificationPlaceholders: List<String>,
    override val target: String,
    override val date: Instant
) : ArtemisNotification<MiscNotificationType>

@Serializable
data class CommunicationArtemisNotification(
    @Serializable(with = CommunicationNotificationTypeDeserializer::class)
    override val type: CommunicationNotificationType,
    override val notificationPlaceholders: List<String>,
    override val target: String,
    override val date: Instant
) : ArtemisNotification<CommunicationNotificationType>

@Serializable
data class UnknownArtemisNotification(
    override val type: UnknownNotificationType,
    override val notificationPlaceholders: List<String>,
    override val target: String,
    override val date: Instant
) : ArtemisNotification<UnknownNotificationType>

private val nameToTypeMapping: Map<String, NotificationType> = (
        StandalonePostCommunicationNotificationType.values().toList() +
                ReplyPostCommunicationNotificationType.values().toList() +
                MiscNotificationType.values().toList()
        ).associateBy { it.name }

object ArtemisNotificationDeserializer :
    JsonContentPolymorphicSerializer<ArtemisNotification<*>>(ArtemisNotification::class) {

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ArtemisNotification<*>> {
        val type: String = (element.jsonObject["type"] as? JsonPrimitive)?.content.orEmpty()
        return when (nameToTypeMapping[type] ?: UnknownNotificationType) {
            is CommunicationNotificationType -> CommunicationArtemisNotification.serializer()
            is MiscNotificationType -> MiscArtemisNotification.serializer()
            UnknownNotificationType -> UnknownArtemisNotification.serializer()
        }
    }
}

object CommunicationNotificationTypeDeserializer :
    KSerializer<CommunicationNotificationType> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("CommunicationNotificationType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): CommunicationNotificationType =
        (nameToTypeMapping[decoder.decodeString()] as? CommunicationNotificationType)
            ?: throw IllegalStateException()

    override fun serialize(encoder: Encoder, value: CommunicationNotificationType) = throw NotImplementedError()
}

val ArtemisNotification<CommunicationNotificationType>.parentId: Long
    get() = NotificationTargetManager.getCommunicationNotificationTarget(
        type.communicationType,
        target
    ).postId

val ArtemisNotification<CommunicationNotificationType>.communicationType: CommunicationType
    get() = when (type) {
        is StandalonePostCommunicationNotificationType, is ReplyPostCommunicationNotificationType -> CommunicationType.QNA_COURSE
    }