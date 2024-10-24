package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread.MetisThreadUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.datetime.Clock
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConversationAnswerMessagesUITest : KoinTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val clientId = 20L

    private val course: Course = Course(id = 1)
    private val conversation = OneToOneChat(id = 2)

    private val post = PostPojo(
        clientPostId = "client-id",
        serverPostId = 12,
        content = "Post content",
        resolved = false,
        updatedDate = null,
        creationDate = Clock.System.now(),
        authorId = clientId,
        title = null,
        authorName = "author name",
        authorRole = UserRole.USER,
        courseWideContext = null,
        tags = emptyList(),
        answers = emptyList(), // TODO: Add your posts here
        reactions = emptyList()

    )

    @Test
    fun `test GIVEN post is not resolved WHEN resolving the post THEN the post is resolved with the clicked answer post`() {
        var resolvedPost: IBasePost? = null

        composeTestRule.setContent {
            MetisThreadUi(
                modifier = Modifier.fillMaxSize(),
                courseId = course.id!!,
                clientId = clientId,
                postDataState = DataState.Success(post),
                conversationDataState = DataState.Success(conversation),
                hasModerationRights = false,
                isAtLeastTutorInCourse = false,
                serverUrl = "",
                initialReplyTextProvider = remember { TestInitialReplyTextProvider() },
                onCreatePost = { CompletableDeferred() },
                onEditPost = { _, _ -> CompletableDeferred() },
                onResolvePost = { post ->
                    resolvedPost = post
                    CompletableDeferred()
                },
                onDeletePost = { CompletableDeferred() },
                onRequestReactWithEmoji = { _, _, _ -> CompletableDeferred() },
                onRequestReload = {},
                onRequestRetrySend = { _, _ -> },
            )
        }

        // TODO: use compose test role to click resolve on the answer post

        // TODO: assert that resolvedPost equals to the post you actually clicked on
    }

    @Test
    fun `test GIVEN post is resolved WHEN un-resolving the post THEN the post is un-resolved`() {
        // TODO: check that the correct callback is called
    }

    @Test
    fun `test GIVEN the post is not resolved and no answer post is resolving THEN the post is shown as not resolved and no answer post is shown as resolving`() {
        // TODO: check that the UI elements are shown correctly
    }

    @Test
    fun `test GIVEN the post is resolved and one answer post is marked as resolving THEN the post is shown as resolved and this answer post is shown as resolving`() {
        // TODO: check that the UI elements are shown correctly
    }
}