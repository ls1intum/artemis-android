package de.tum.informatics.www1.artemis.native_app.core.websocket.impl

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.categories.Category

@Category(UnitTest::class)
class WebsocketTopicTest {

    @Test
    fun `course wide conversation topic uses the communication destination`() {
        assertEquals(
            "/topic/communication/courses/42",
            WebsocketTopic.getCourseWideConversationUpdateTopic(courseId = 42)
        )
    }

    @Test
    fun `normal conversation topic is the per user notification destination`() {
        assertEquals(
            "/topic/user/7/notifications/conversations",
            WebsocketTopic.getNormalConversationUpdateTopic(userId = 7)
        )
    }

    @Test
    fun `conversation meta update topic uses the communication destination`() {
        assertEquals(
            "/user/topic/communication/courses/42/conversations/user/7",
            WebsocketTopic.getConversationMetaUpdateTopic(courseId = 42, userId = 7)
        )
    }
}
