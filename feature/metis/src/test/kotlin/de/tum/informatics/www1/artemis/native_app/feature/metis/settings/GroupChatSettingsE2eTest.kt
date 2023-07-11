package de.tum.informatics.www1.artemis.native_app.feature.metis.settings

import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.testServerUrl
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user1Username
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user2Username
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user3Username
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog
import kotlin.test.assertEquals

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class GroupChatSettingsE2eTest : ConversationSettingsBaseE2eTest() {

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can leave group chat`() {
        val groupChat = runBlocking {
            conversationService.createGroupChat(
                courseId = course.id!!,
                groupMembers = listOf(user1Username, user2Username, user3Username),
                authToken = getAdminAccessToken(),
                serverUrl = testServerUrl
            )
                .orThrow("Could not create group chat")
        }

        canLeaveConversationTestImpl(groupChat)
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can change group chat name`() {
        ShadowLog.stream = System.out

        val groupChat = runBlocking {
            conversationService.createGroupChat(
                courseId = course.id!!,
                groupMembers = listOf(user1Username, user2Username, user3Username),
                authToken = getAdminAccessToken(),
                serverUrl = testServerUrl
            )
                .orThrow("Could not create group chat")
        }

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
}