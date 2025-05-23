package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.saved_posts

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.ui.test.BottomSheetClickWorkaroundTheme
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.saved_posts.ui.SavedPostsScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.MetisModificationTask
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ISavedPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostStatus
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.getStringResId
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith

@Category(UnitTest::class)
@RunWith(AndroidJUnit4::class)
class SavedPostsScreenUiTest : BaseComposeTest() {

    private val postContent = "Post content"

    @Test
    fun `test GIVEN a single Saved Post WHEN displaying the screen THEN the post is displayed`() {
        setupUi()

        composeTestRule.onNodeWithText(postContent)
            .assertExists()
    }

    @Test
    fun `test GIVEN the inProgress screen WHEN pressing 'Complete' THEN the post is marked as completed`() {
        var changedStatus: SavedPostStatus? = null

        setupUi(
            onChangeStatus = { _, newStatus ->
                changedStatus = newStatus
                CompletableDeferred(null)
            }
        )

        composeTestRule.onNodeWithText(context.getString(R.string.saved_posts_action_mark_as_completed))
            .performClick()
        assertEquals(SavedPostStatus.COMPLETED, changedStatus)
    }

    @Test
    fun `test GIVEN a single Saved Post WHEN long pressing the post THEN bottomSheet with remaining change_status and remove action buttons are displayed`() {
        setupUi()

        composeTestRule.onNodeWithText(postContent)
            .performSemanticsAction(SemanticsActions.OnLongClick)

        composeTestRule.onNodeWithText(getActionButtonTextForStatus(SavedPostStatus.COMPLETED))
            .assertExists()
        composeTestRule.onNodeWithText(getActionButtonTextForStatus(SavedPostStatus.ARCHIVED))
            .assertExists()
        composeTestRule.onNodeWithText(getActionButtonTextForStatus(SavedPostStatus.IN_PROGRESS))
            .assertDoesNotExist()

        composeTestRule.onNodeWithText(context.getString(R.string.saved_posts_action_remove))
            .assertExists()
    }

    @Test
    fun `test GIVEN a single Saved Post WHEN pressing 'Remove from saved' THEN the callback is called`() {
        var removeCalled = false

        setupUi(
            onRemoveFromSavedPosts = {
                removeCalled = true
                CompletableDeferred(null)
            }
        )

        composeTestRule.onNodeWithText(postContent)
            .performSemanticsAction(SemanticsActions.OnLongClick)
        composeTestRule.onNodeWithText(context.getString(R.string.saved_posts_action_remove))
            .performClick()

        assert(removeCalled)
    }



    private fun getActionButtonTextForStatus(status: SavedPostStatus): String {
        val statusString = context.getString(status.getStringResId())
        return context.getString(R.string.saved_posts_action_change_status, statusString)
    }

    private fun setupUi(
        savedPost: ISavedPost = createSavedPost(),
        onChangeStatus: (ISavedPost, SavedPostStatus) -> MetisModificationTask = { _, _ -> CompletableDeferred() },
        onRemoveFromSavedPosts: (ISavedPost) -> MetisModificationTask = { CompletableDeferred() }
    ) {
        composeTestRule.setContent {
            BottomSheetClickWorkaroundTheme {
                SavedPostsScreen(
                    modifier = Modifier,
                    savedPostsChatListItemsDataState = DataState.Success(
                        listOf(
                            ChatListItem.PostItem.SavedItem.SavedPost(
                                savedPost
                            )
                        )
                    ),
                    onRequestReload = {},
                    onNavigateToPost = { _ -> },
                    onChangeStatus = onChangeStatus,
                    onRemoveFromSavedPosts = onRemoveFromSavedPosts,
                    onSidebarToggle = {}
                )
            }
        }
    }

    private fun createSavedPost(
        status: SavedPostStatus = SavedPostStatus.IN_PROGRESS
    ): ISavedPost {
        return SavedPostsTestUtil.fromStandalonePost(
            post = StandalonePost(id = 1L, content = postContent),
            status = status
        )
    }
}