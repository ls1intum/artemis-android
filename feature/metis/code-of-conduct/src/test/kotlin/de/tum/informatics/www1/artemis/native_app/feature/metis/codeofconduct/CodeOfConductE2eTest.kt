package de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.service.CodeOfConductService
import de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.service.impl.CodeOfConductServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.ui.CodeOfConductFacadeUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.ui.CodeOfConductViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metistest.ConversationBaseTest
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class CodeOfConductE2eTest : ConversationBaseTest() {

    private companion object {
        private const val ERROR_MESSAGE_COC = "Could not load code of conduct from server"
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `test can accept code of conduct without ui`() = runTest(testDispatcher) {
        val codeOfConductService: CodeOfConductService = CodeOfConductServiceImpl(get())

        val isCocAccepted = codeOfConductService
            .getIsCodeOfConductAccepted(course.id!!, testServerUrl, accessToken)
            .orThrow(ERROR_MESSAGE_COC)

        assertFalse(isCocAccepted, "Initial code of conduct should not be accepted.")

        codeOfConductService
            .acceptCodeOfConduct(course.id!!, testServerUrl, accessToken)
            .orThrow("Could not accept course of conduct.")

        val newIsCocAccepted = codeOfConductService
            .getIsCodeOfConductAccepted(course.id!!, testServerUrl, accessToken)
            .orThrow(ERROR_MESSAGE_COC)

        assertTrue(newIsCocAccepted, "Code of conduct is still not accepted.")
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `test fetches correct responsible users`() = runTest(testDispatcher) {
        val codeOfConductService: CodeOfConductService = CodeOfConductServiceImpl(get())

        val responsibleUsers = codeOfConductService
            .getResponsibleUsers(course.id!!, testServerUrl, accessToken)
            .orThrow("Could not fetch responsible users")

        // Expect Test User 1, 2 and 3
        assertEquals(
            3,
            responsibleUsers.size,
            "Expected three responsible users, but got wrong amount"
        )
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `test can fetch code of conduct text`() = runTest(testDispatcher) {
        val codeOfConductService: CodeOfConductService = CodeOfConductServiceImpl(get())
        codeOfConductService.getCodeOfConductTemplate(
            courseId = course.id!!,
            testServerUrl,
            accessToken
        )
            .orThrow("Could not fetch code of conduct text")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test(timeout = DefaultTestTimeoutMillis)
    fun `test can accept code of conduct in ui`() {
        val viewModel =
            CodeOfConductViewModel(
                courseId = course.id!!,
                codeOfConductStorageService = get(),
                codeOfConductService = get(),
                networkStatusProvider = get(),
                serverConfigurationService = get(),
                accountService = get(),
                courseService = get(),
                coroutineContext = testDispatcher
            )

        var accepted = false

        composeTestRule.setContent {
            CodeOfConductFacadeUi(
                modifier = Modifier.fillMaxSize(),
                codeOfConductViewModel = viewModel,
                onCodeOfConductAccepted = { accepted = true }
            )
        }

        composeTestRule.waitUntilExactlyOneExists(
            hasText(context.getString(R.string.code_of_conduct_button_accept)),
            DefaultTimeoutMillis
        )

        composeTestRule
            .onNodeWithText(context.getString(R.string.code_of_conduct_button_accept))
            .assertExists("Accept button is not present.")
            .performScrollTo()
            .performClick()

        composeTestRule.waitForIdle()

        assertTrue(accepted, "Code of conduct was not accepted.")
    }
}
