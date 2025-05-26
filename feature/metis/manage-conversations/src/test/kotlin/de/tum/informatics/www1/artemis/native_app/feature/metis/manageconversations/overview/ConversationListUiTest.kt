package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.overview

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.CollapsingContentState
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.ConversationList
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.model.ConversationCollections
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith

@Category(UnitTest::class)
@RunWith(AndroidJUnit4::class)
class ConversationListUiTest : BaseComposeTest() {


    @Test
    fun `test GIVEN the conversationList WHEN always THEN display the saved posts section`() {
        setupUi()

        composeTestRule
            .onNodeWithText(context.getString(R.string.conversation_overview_section_saved_posts))
            .assertExists()
    }

    private fun setupUi(
        conversations: ConversationCollections = createEmptyConversationCollections()
    ) {
        composeTestRule.setContent {
            ConversationList(
                modifier = Modifier,
                toggleFavoritesExpanded = {},
                toggleGeneralsExpanded = {},
                toggleExercisesExpanded = {},
                toggleLecturesExpanded = {},
                toggleExamsExpanded = {},
                toggleGroupChatsExpanded = {},
                togglePersonalConversationsExpanded = {},
                toggleHiddenExpanded = {},
                conversationCollections = conversations,
                collapsingContentState = CollapsingContentState(),
                onNavigateToConversation = {},
                onNavigateToSavedPosts = {},
                onToggleMarkAsFavourite = { _, _ -> },
                onToggleHidden = { _, _ -> },
                onToggleMuted = { _, _ -> },
                trailingContent = {},
                selectedConversationId = null,
                listState = rememberLazyListState(),

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
            examChannels = ConversationCollections.ConversationCollection(emptyList(), isExpanded = true)
        )
    }
}