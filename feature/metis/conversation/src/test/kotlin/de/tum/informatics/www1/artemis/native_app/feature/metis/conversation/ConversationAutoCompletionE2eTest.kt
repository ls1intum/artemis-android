package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation

import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.login.service.network.RegisterService
import de.tum.informatics.www1.artemis.native_app.feature.metistest.ConversationBaseTest
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import org.robolectric.util.Logger
import java.util.Locale
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds


@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class ConversationAutoCompletionE2eTest : ConversationBaseTest() {

    private val registerService: RegisterService get() = get()

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `auto-completion suggests users`() {
        val usernames = (0 until 3).map {
            "user$it"
        }

        runTest(timeout = DefaultTimeoutMillis.milliseconds * 4) {
            // Create users
            val createdUsers = usernames.map { username ->
                registerService.register(
                    account = User(
                        firstName = "firstName",
                        lastName = "lastName",
                        username = username,
                        email = "email@domain.com",
                        password = "password",
                        langKey = Locale.getDefault().toLanguageTag()
                    ),
                    serverUrl = testServerUrl,
                ).orThrow("Could not create user with username $username")
            }

            // Simulate typing a channel name
            val typedText = "@user"
            val autoCompleteSuggestions = conversationService.searchForCourseMembers(
                courseId = course.id!!,
                query = typedText,
                authToken = accessToken,
                serverUrl = testServerUrl
            ).orThrow("Could not get auto-complete suggestions")

            Logger.info("Auto-complete suggestions: $autoCompleteSuggestions")

            assertEquals(usernames.size, autoCompleteSuggestions.size)

            // Verify that the suggestions contain the expected user
            usernames.forEach { username ->
                assertTrue(
                    autoCompleteSuggestions.any { it.username == username },
                    "Auto-complete suggestions do not contain user $username"
                )
            }
        }
    }
}