package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.overview

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasContentDescriptionExactly
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.CollapsingContentState
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ConversationCollections
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.ConversationList
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostStatus
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.getStringResId
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith

@Category(UnitTest::class)
@RunWith(AndroidJUnit4::class)
class ConversationListUiTest : BaseComposeTest() {


    @Test
    fun `test GIVEN the conversationList WHEN always THEN display the saved posts section and its elements`() {
        setupUi()

        composeTestRule
            .onNodeWithText(context.getString(R.string.conversation_overview_section_saved_posts))
            .assertExists()

        composeTestRule
            .onNodeWithText(context.getString(SavedPostStatus.IN_PROGRESS.getStringResId()))
            .assertExists()
        composeTestRule
            .onNodeWithText(context.getString(SavedPostStatus.COMPLETED.getStringResId()))
            .assertExists()
        composeTestRule
            .onNodeWithText(context.getString(SavedPostStatus.ARCHIVED.getStringResId()))
            .assertExists()
    }

    @Test
    fun `test GIVEN the conversationList WHEN always THEN the saved posts elements should not show the ShowActions IconButton`() {
        setupUi()

        composeTestRule
            .onNodeWithText(context.getString(SavedPostStatus.IN_PROGRESS.getStringResId()))
            .assertDoesNotShowShowActionsIconButton()
        composeTestRule
            .onNodeWithText(context.getString(SavedPostStatus.COMPLETED.getStringResId()))
            .assertDoesNotShowShowActionsIconButton()
        composeTestRule
            .onNodeWithText(context.getString(SavedPostStatus.ARCHIVED.getStringResId()))
            .assertDoesNotShowShowActionsIconButton()
    }

    private fun SemanticsNodeInteraction.assertDoesNotShowShowActionsIconButton() {
        assert(!hasAnyChild(hasContentDescriptionExactly(context.getString(R.string.conversation_overview_conversation_item_show_actions))))
    }


    private fun setupUi(
        isSavedPostSectionExpanded: Boolean = true,
        conversations: ConversationCollections = createEmptyConversationCollections()
    ) {
        composeTestRule.setContent {
            ConversationList(
                modifier = Modifier,
                isSavedPostsSectionExpanded = isSavedPostSectionExpanded,
                toggleFavoritesExpanded = {},
                toggleGeneralsExpanded = {},
                toggleExercisesExpanded = {},
                toggleLecturesExpanded = {},
                toggleExamsExpanded = {},
                toggleGroupChatsExpanded = {},
                togglePersonalConversationsExpanded = {},
                toggleHiddenExpanded = {},
                toggleSavedPostsExpanded = {},
                toggleRecentExpanded = {},
                conversationCollections = conversations,
                collapsingContentState = CollapsingContentState(),
                onNavigateToConversation = {},
                onNavigateToSavedPosts = {},
                onToggleMarkAsFavourite = { _, _ -> },
                onToggleHidden = { _, _ -> },
                onToggleMuted = { _, _ -> },
                trailingContent = {}
            )
        }
    }

    private fun createEmptyConversationCollections(): ConversationCollections {
        return ConversationCollections(
            favorites = ConversationCollections.ConversationCollection(emptyList(), isExpanded = true),
            channels = ConversationCollections.ConversationCollection(emptyList(), isExpanded = true),
            groupChats = ConversationCollections.ConversationCollection(emptyList(), isExpanded = true),
            directChats = ConversationCollections.ConversationCollection(emptyList(), isExpanded = true),
            hidden = ConversationCollections.ConversationCollection(emptyList(), isExpanded = true),
            exerciseChannels = ConversationCollections.ConversationCollection(emptyList(), isExpanded = true),
            lectureChannels = ConversationCollections.ConversationCollection(emptyList(), isExpanded = true),
            examChannels = ConversationCollections.ConversationCollection(emptyList(), isExpanded = true),
            recentChannels = ConversationCollections.ConversationCollection(emptyList(), isExpanded = true)
        )
    }
}