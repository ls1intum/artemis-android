package de.tum.informatics.www1.artemis.native_app.feature.metis.messages

import androidx.compose.ui.test.ExperimentalTestApi
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.testServerUrl
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.AnswerPost
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.util.Logger
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.milliseconds

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class ConversationAnswerMessagesE2eTest : ConversationMessagesBaseTest() {

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `shows existing message with answer posts`() {
        val answerPostContents = (0 until 3).map {
            "answer post content $it"
        }

        runTest(timeout = DefaultTimeoutMillis.milliseconds * 4) {
            val post = metisModificationService.createPost(
                context = metisContext,
                post = createPost("test message"),
                serverUrl = testServerUrl,
                authToken = accessToken
            ).orThrow("Could not create message")

            val answerPosts = answerPostContents.map { replyText ->
                metisModificationService.createAnswerPost(
                    context = metisContext,
                    post = AnswerPost(
                        creationDate = Clock.System.now(),
                        content = replyText,
                        post = post
                    ),
                    serverUrl = testServerUrl,
                    authToken = accessToken
                ).orThrow("Could not create answer message with text $post")
            }

            val downloadedPost = metisService
                .getPost(metisContext, post.serverPostId, testServerUrl, accessToken)
                .orThrow("Could not download relevant post")

            Logger.info("Downloaded post = $downloadedPost")

            assertEquals(answerPostContents.size, downloadedPost.answers.orEmpty().size)

            answerPosts.forEach { answerPost ->
                val downloadedAnswerPost = assertNotNull(
                    downloadedPost.answers.orEmpty().firstOrNull { it.id == answerPost.id },
                    "Answer post $answerPost not found in downloaded post"
                )

                assertEquals(answerPost.content, downloadedAnswerPost.content, "Content does not match")
            }
        }
    }
}
