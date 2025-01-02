package de.tum.informatics.www1.artemis.native_app.core.websocket.util

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.hildan.krossbow.stomp.frame.FrameBody
import org.hildan.krossbow.stomp.frame.StompFrame
import org.hildan.krossbow.stomp.headers.StompMessageHeaders
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@Serializable
data class TestObject(
    private val value1: String
)

@OptIn(ExperimentalEncodingApi::class)
@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class WebsocketCompressionUtilTest {

    private val jsonConfig = Json
    private val testObject = TestObject("test")
    private val message = Json.encodeToString(TestObject.serializer(), testObject)

    @Test
    fun `test GIVEN a non-compressed message without a compression header WHEN calling the util THEN the deserialized message is returned`() {
        val stompFrame = StompFrame.Message(
            headers = createHeaders(),
            body = FrameBody.Text(message)
        )

        val deserialized = WebsocketCompressionUtil.deserializeMessage(stompFrame, jsonConfig, TestObject.serializer())

        assertEquals(testObject, deserialized)
    }

    @Test
    fun `test GIVEN a compressed message with a compression header WHEN calling the util THEN the deserialized message is returned`() {
        val compressedMessage = compressGzip(message.toByteArray())
        val base64 = Base64.Default.encode(compressedMessage)
        val stompFrame = StompFrame.Message(
            headers = createHeaders(mapOf(WebsocketCompressionUtil.COMPRESSION_HEADER to "true")),
            body = FrameBody.Text(base64)
        )

        val deserialized = WebsocketCompressionUtil.deserializeMessage(stompFrame, jsonConfig, TestObject.serializer())

        assertEquals(testObject, deserialized)
    }

    private fun createHeaders(customHeaders: Map<String, String> = mapOf()) = StompMessageHeaders(
        destination = "dest",
        messageId = "msgId",
        subscription = "subscription"
    ) {
        ack = null
        setAll(
            customHeaders
        )
    }

    private fun compressGzip(data: ByteArray): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).use { it.write(data) }
        return bos.toByteArray()
    }

}
