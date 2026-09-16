package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.impl.CreateReactionDto
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.impl.PostingType
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * The reaction endpoint takes the posting as a flat id, and that id does not identify it: messages
 * and answer messages are numbered independently on the server, so the same value regularly denotes
 * one of each. These assert the wire format the server reads the kind from.
 */
@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class CreateReactionDtoTest {

    private val json = Json { encodeDefaults = true }

    @Test
    fun `a reaction on a message names the message`() {
        val body = json.encodeToString(
            CreateReactionDto(emojiId = "joy", relatedPostId = 3L, postingType = PostingType.POST)
        )

        assertEquals("""{"emojiId":"joy","relatedPostId":3,"postingType":"POST"}""", body)
    }

    @Test
    fun `a reaction on an answer message names the answer message`() {
        val body = json.encodeToString(
            CreateReactionDto(emojiId = "joy", relatedPostId = 3L, postingType = PostingType.ANSWER)
        )

        assertEquals("""{"emojiId":"joy","relatedPostId":3,"postingType":"ANSWER"}""", body)
    }
}
