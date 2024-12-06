package de.tum.informatics.www1.artemis.native_app.core.websocket.util

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.hildan.krossbow.stomp.frame.FrameBody
import org.hildan.krossbow.stomp.frame.StompFrame
import org.hildan.krossbow.stomp.headers.StompMessageHeaders
import org.junit.Assert.assertEquals
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream
import kotlin.test.Test


@Serializable
data class TestObject(
    private val value1: String
)

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class WebsocketCompressionUtilTest {

    private val jsonConfig = Json

    @Test
    fun `test GIVEN a non-compressed message without a compression header WHEN calling the util THEN the deserialized message is returned`() {
        val message = "{ \"value1\": \"test\" }"
        val stompFrame = StompFrame.Message(
            headers = createHeaders(),
            body = FrameBody.Text(message)
        )

        val deserialized = WebsocketCompressionUtil.deserializeMessage(stompFrame, jsonConfig, TestObject.serializer())

        assertEquals(TestObject("test"), deserialized)
    }

    @Test
    fun `test GIVEN a compressed message with a compression header WHEN calling the util THEN the deserialized message is returned`() {
        val originalMessage = "{ \"value1\": \"test\" }"
        val compressedMessage = compressGzip(originalMessage.toByteArray())
        val stompFrame = StompFrame.Message(
            headers = createHeaders(mapOf("compressed" to "true")),
            body = FrameBody.Binary(compressedMessage)
        )

        val deserialized = WebsocketCompressionUtil.deserializeMessage(stompFrame, jsonConfig, TestObject.serializer())

        assertEquals(TestObject("test"), deserialized)
    }

    private fun createHeaders(customHeaders: Map<String, String> = mapOf()) = StompMessageHeaders(
        destination = "dest",
        messageId = "msgId",
        subscription = "subscription",
        customHeaders = customHeaders
    )

    private fun compressGzip(data: ByteArray): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).use { it.write(data) }
        return bos.toByteArray()
    }

}
