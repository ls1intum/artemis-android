package de.tum.informatics.www1.artemis.native_app.feature.push.notification_model

import kotlinx.datetime.Instant
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = ArtemisNotificationDeserializer::class)
sealed interface ArtemisNotification<T : NotificationType>  {
    val date: Instant
    val version: Int
    val courseNotificationDTO: CourseNotificationDTO
}

@Serializable
data class GeneralArtemisNotification(
    @Serializable(with = SafeInstantSerializer::class)
    override val date: Instant,
    override val version: Int,
    @SerialName("courseNotificationDTO")
    override val courseNotificationDTO: CourseNotificationDTO
) : ArtemisNotification<GeneralNotificationType>

@Serializable
data class CommunicationArtemisNotification(
    @Serializable(with = SafeInstantSerializer::class)
    override val date: Instant,
    override val version: Int,
    @SerialName("courseNotificationDTO")
    override val courseNotificationDTO: CourseNotificationDTO,
) : ArtemisNotification<CommunicationNotificationType>

@Serializable
data class UnknownArtemisNotification(
    @Serializable(with = SafeInstantSerializer::class)
    override val date: Instant,
    override val version: Int,
    @SerialName("courseNotificationDTO")
    override val courseNotificationDTO: CourseNotificationDTO
) : ArtemisNotification<UnknownNotificationType>

object ArtemisNotificationDeserializer :
    JsonContentPolymorphicSerializer<ArtemisNotification<*>>(ArtemisNotification::class) {

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ArtemisNotification<*>> {
        val courseNotificationDTO = element.jsonObject["courseNotificationDTO"]?.jsonObject
        val categoryString = courseNotificationDTO?.get("category")?.jsonPrimitive?.contentOrNull

        val category = try {
            NotificationCategory.valueOf(categoryString ?: "")
        } catch (e: IllegalArgumentException) {
            NotificationCategory.UNKNOWN
        }

        return when (category) {
            NotificationCategory.COMMUNICATION ->
                CommunicationArtemisNotification.serializer()

            NotificationCategory.GENERAL ->
                GeneralArtemisNotification.serializer()

            NotificationCategory.UNKNOWN ->
                UnknownArtemisNotification.serializer()
        }
    }
}

val ArtemisNotification<CommunicationNotificationType>.parentId: Long
    get() = NotificationTargetGenerator.generateCommunicationTarget(courseNotificationDTO).postId

// This serializer is needed to handle the incompatible date format sent by the server, which is not compatible with the default Instant serializer.
object SafeInstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("SafeInstant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        val raw = decoder.decodeString()            // eg "2025-05-04T11:28:18.762036099Z[Etc/UTC]"
        val cleaned = raw.substringBefore('[')
        return Instant.parse(cleaned)
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }
}