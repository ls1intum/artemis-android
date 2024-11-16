package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation

import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user1Username
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user2Username
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user3Username
import de.tum.informatics.www1.artemis.native_app.feature.metistest.ConversationBaseTest
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.util.Logger
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds


@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class ConversationAutoCompletionE2eTest : ConversationBaseTest() {

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `test GIVEN users are registered in a course WHEN requesting auto complete users THEN the registered users are returned`() {
        val users = listOf(
            User(username = user1Username),
            User(username = user2Username),
            User(username = user3Username)
        )

        runTest(timeout = DefaultTimeoutMillis.milliseconds) {

            val typedText = "user"
            val autoCompleteSuggestions = conversationService.searchForCourseMembers(
                courseId = course.id!!,
                query = typedText,
                authToken = accessToken,
                serverUrl = testServerUrl
            ).orThrow("Could not get auto-complete suggestions")

            Logger.info("Auto-complete suggestions: $autoCompleteSuggestions")

            assertEquals(users.size, autoCompleteSuggestions.size)

            users.forEach { user ->
                assertTrue(
                    autoCompleteSuggestions.any { it.username == user.username },
                    "Auto-complete suggestions do not contain user $user"
                )
            }
        }
    }
}