package de.tum.informatics.www1.artemis.native_app.core.websocket.impl

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import org.hildan.krossbow.stomp.frame.StompFrame

object WebsocketCompressionUtil {

    /**
     * Deserializes the message body of a [StompFrame.Message] into a Kotlin object. It decompresses
     * the message body if the message is marked as compressed.
     *
     * @param message The message to deserialize.
     * @param jsonConfig The JSON configuration to use for deserialization.
     * @param deserializer The deserialization strategy to use.
     * @return The deserialized object.
     */
    fun <T> deserializeMessage(
        message: StompFrame.Message,
        jsonConfig: Json,
        deserializer: DeserializationStrategy<T>
    ): T {
        val bodyJson: String = if (message.headers["compressed"] == "true") {
            val compressed = message.body?.bytes ?: error("Message body should not be null")
            val decompressed = decompress(compressed)
            String(decompressed)
        } else message.bodyAsText

        return jsonConfig.decodeFromString(deserializer, bodyJson)
    }

    private fun decompress(compressed: ByteArray): ByteArray {
        return compressed   // TODO
    }
}