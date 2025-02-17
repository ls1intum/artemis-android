package de.tum.informatics.www1.artemis.native_app.feature.faq

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.Faq
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.FaqState
import de.tum.informatics.www1.artemis.native_app.feature.faq.ui.overview.FaqOverviewUi
import de.tum.informatics.www1.artemis.native_app.feature.faq.ui.overview.FaqOverviewViewModel
import de.tum.informatics.www1.artemis.native_app.feature.faq.ui.overview.TEST_TAG_FAQ_OVERVIEW_SEARCH
import de.tum.informatics.www1.artemis.native_app.feature.faq.ui.overview.testTagForFaq
import de.tum.informatics.www1.artemis.native_app.feature.faq_test.FaqRepositoryMock
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith

@Category(UnitTest::class)
@RunWith(AndroidJUnit4::class)
class FaqOverviewUiTest : BaseComposeTest() {

    private val courseId = 123L

    private val faq1 = Faq(
        id = 1,
        questionTitle = "Title1",
        questionAnswer = "Answer1",
        categories = emptyList(),
        faqState = FaqState.ACCEPTED
    )

    private val faq2 = Faq(
        id = 2,
        questionTitle = "Title2",
        questionAnswer = "Answer2",
        categories = emptyList(),
        faqState = FaqState.ACCEPTED
    )

    @Test
    fun `test GIVEN faqs WHEN displaying the overview THEN the title and description should be visible`() {
        setupUiAndViewModel(listOf(faq1))

        faq1.assertIsVisible()
    }

    @Test
    fun `test GIVEN faqs WHEN searching THEN only faqs matching the search should be visible`() {
        setupUiAndViewModel(listOf(faq1, faq2))

        faq1.assertIsVisible()
        faq2.assertIsVisible()

        searchFor(faq1.questionTitle)

        faq1.assertIsVisible()
        faq2.assertIsHidden()

        searchFor(faq2.questionAnswer)

        faq1.assertIsHidden()
        faq2.assertIsVisible()
    }



    private fun setupUiAndViewModel(
        faqs: List<Faq>
    ) {
        val viewModel = FaqOverviewViewModel(
            courseId = courseId,
            faqRepository = FaqRepositoryMock(faqs = faqs),
            coroutineContext = testDispatcher
        )

        composeTestRule.setContent {
            FaqOverviewUi(
                viewModel = viewModel,
                onNavigateToFaq = {}
            )
        }
    }

    private fun Faq.assertIsVisible() {
        composeTestRule.onNodeWithTag(testTagForFaq(this)).assertIsDisplayed()
    }

    private fun Faq.assertIsHidden() {
        composeTestRule.onNodeWithTag(testTagForFaq(this)).assertDoesNotExist()
    }

    private fun searchFor(query: String) {
        val searchInput = composeTestRule.onNodeWithTag(TEST_TAG_FAQ_OVERVIEW_SEARCH)
        searchInput.performTextClearance()
        searchInput.performTextInput(query)
        composeTestRule.waitForIdle()       // Wait until LazyColumn animateItem() animation is done
    }
}