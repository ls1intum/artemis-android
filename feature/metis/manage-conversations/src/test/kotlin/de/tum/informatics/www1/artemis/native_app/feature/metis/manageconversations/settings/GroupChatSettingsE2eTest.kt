package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.settings

import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user1Username
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user2Username
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user3Username
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
internal class GroupChatSettingsE2eTest : ConversationSettingsBaseE2eTest() {

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can leave group chat`() {
        val groupChat = createGroupChat()

        canLeaveConversationTestImpl(groupChat)
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can change group chat name`() {
        val groupChat = createGroupChat()

        val newTitle = "testgroupchat"

        changeConversationDetailsTestImpl(
            conversation = groupChat,
            performChanges = {
                changeTitleText(groupChat.name, newTitle)
            },
            verifyChanges = { updatedGroupChat ->
                assertEquals(newTitle, updatedGroupChat.name)
            }
        )
    }

    private fun createGroupChat(): GroupChat {
        return runBlockingWithTestTimeout {
            conversationService.createGroupChat(
                courseId = course.id!!,
                groupMembers = listOf(user1Username, user2Username, user3Username),
                authToken = getAdminAccessToken(),
                serverUrl = testServerUrl
            )
                .orThrow("Could not create group chat")
        }
    }
}