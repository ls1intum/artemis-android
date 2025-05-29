package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.overview

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onNodeWithText
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.CollapsingContentState
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.ConversationList
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.model.ConversationCollections
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class ConversationListUiTest : BaseComposeTest() {

    @Test
    fun `test GIVEN the conversationList WHEN always THEN display the saved posts section`() {
        setupUi()

        composeTestRule
            .onNodeWithText(context.getString(R.string.conversation_overview_section_saved_posts))
            .assertExists()
    }

    private fun setupUi(
        conversations: ConversationCollections = ConversationCollections(emptyList())
    ) {
        composeTestRule.setContent {
            ConversationList(
                modifier = Modifier,
                conversationCollections = conversations,
                collapsingContentState = CollapsingContentState(),
                onToggleSectionExpanded = { _ -> },
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
}