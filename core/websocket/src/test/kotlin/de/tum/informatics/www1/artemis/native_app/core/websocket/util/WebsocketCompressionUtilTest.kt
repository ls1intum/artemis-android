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
import kotlin.test.assertContains


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

    // These real world example where obtained by copying a compressed payload observed in the webapp
    // For more details on how to obtain these payloads, see https://github.com/ls1intum/Artemis/pull/10087
    private val realWorldCompressedPayload = "H4sIAAAAAAAA/01RXU/jMBD8L35OkeN85w3S6ohUAaJBSHfiwXHc3KLEDv4Ql0P976zp9cST7Znd2dnxrw8CA6lJVdA8jYukwEuGZ5ySiCg+S+Qemnb/fGApy7rdodvfN9f7pt382CXZtv2JZb2HabgepXKk/jhFZOHGgYCFO9CqRfUv8YgI7Y2VAYhpHhH5RxoBZ6BiNCJGOrM22gcdfC4GtAG3kpoFbtEWnDZrq44a53xD7s42FwHTuw02nbRu0oJPAjajTLIB/pLvEt26hIanw+4RcWdgHKWRw8364O3vTl8oXOVV9x3MoMbLVOv7GazFxbbcBRFGWbah8YYVHa3qJKvj4grTYzmrqjykg15gxtph681XIrhPdfqXWqPVEcYgLPQ8g7vlwcBNoFA7rXI2pHlSDH0WH2lZCV6KXlBOWZnwtC8Z74u4FDjFCr5TvJ8k9jnjJSLyzeOXAJ86tPDolf1fcOSTxQr0JbV3Bym0Gixmfjq9fAJGHC14EAIAAA=="
    private val realWorldUncompressedPayloadSubstring = "[{\"id\":\"970641737970517314\","

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

    @Test
    fun `test GIVEN a real world compressed payload WHEN calling the util THEN the deserialized message is returned`() {
        val stompFrame = StompFrame.Message(
            headers = createHeaders(mapOf(WebsocketCompressionUtil.COMPRESSION_HEADER to "true")),
            body = FrameBody.Text(realWorldCompressedPayload)
        )

        val body = WebsocketCompressionUtil.extractPotentiallyCompressedBody(stompFrame)

        assertContains(body, realWorldUncompressedPayloadSubstring)
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
