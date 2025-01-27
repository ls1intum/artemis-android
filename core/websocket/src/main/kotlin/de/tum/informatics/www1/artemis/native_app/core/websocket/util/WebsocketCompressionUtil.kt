package de.tum.informatics.www1.artemis.native_app.core.websocket.util

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import org.hildan.krossbow.stomp.frame.FrameBody
import org.hildan.krossbow.stomp.frame.StompFrame
import java.util.zip.GZIPInputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.text.Charsets.UTF_8

@OptIn(ExperimentalEncodingApi::class)
object WebsocketCompressionUtil {

    const val COMPRESSION_HEADER = "X-Compressed"

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
        val bodyJson = extractPotentiallyCompressedBody(message)
        return jsonConfig.decodeFromString(deserializer, bodyJson)
    }

    internal fun extractPotentiallyCompressedBody(message: StompFrame.Message): String {
        if (message.headers[COMPRESSION_HEADER] != "true") {
            return message.bodyAsText
        }

        val frameBody = message.body as? FrameBody.Text ?: error("Expected text body")
        val compressedInBase64 = frameBody.text
        val compressed = Base64.Default.decode(compressedInBase64)
        return ungzip(compressed)
    }

    private fun ungzip(compressed: ByteArray): String {
        // From: https://gist.github.com/sgdan/eaada2f243a48196c5d4e49a277e3880
        return GZIPInputStream(compressed.inputStream()).bufferedReader(UTF_8).use { it.readText() }
    }
}