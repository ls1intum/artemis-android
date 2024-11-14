package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation

import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.AnswerPostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import kotlinx.datetime.Clock
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class MarkdownTextfieldVisibilityUITest : BaseComposeTest() {

    private val clientId = 20L

    private val course: Course = Course(id = 1)
    private val conversation = OneToOneChat(id = 2)

    private val answers = (0..2).map { index ->
        AnswerPostPojo(
            parentPostId = "client-id",
            postId = "answer-client-id-$index",
            resolvesPost = false,
            basePostingCache = AnswerPostPojo.BasePostingCache(
                serverPostId = index.toLong(),
                authorId = clientId,
                creationDate = Clock.System.now(),
                updatedDate = null,
                content = "Answer Post content $index",
                authorRole = UserRole.USER,
                authorName = "author name"
            ),
            reactions = emptyList(),
            serverPostIdCache = AnswerPostPojo.ServerPostIdCache(
                serverPostId = index.toLong()
            )
        )
    }

    private val posts = (0..2).map { index ->
        PostPojo(
            clientPostId = "client-id-$index",
            serverPostId = index.toLong(),
            content = "Post content $index",
            resolved = false,
            updatedDate = null,
            creationDate = Clock.System.now(),
            authorId = clientId,
            title = null,
            authorName = "author name",
            authorRole = UserRole.USER,
            courseWideContext = null,
            tags = emptyList(),
            answers = if (index == 0) answers else emptyList(),
            reactions = emptyList()
        )
    }

    @Test
    fun `test if the keyboard covers the markdown text field in the thread ui filled with three answer posts`() { }

    @Test
    fun `test if the keyboard covers the markdown text field in the chat list filled with three posts`() { }

    private fun setupThreadUi() {
        composeTestRule.setContent {

        }
    }

    private fun setupChatUi() {
        composeTestRule.setContent {

        }
    }
}
