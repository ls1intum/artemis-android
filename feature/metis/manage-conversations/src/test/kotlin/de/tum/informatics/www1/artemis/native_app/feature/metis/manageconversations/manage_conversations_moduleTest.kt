package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.lifecycle.ViewModel
import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.browse_channels.BrowseChannelsViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_channel.CreateChannelViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_personal_conversation.CreatePersonalConversationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.ConversationOverviewViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.add_members.ConversationAddMembersViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.members.ConversationMembersViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.overview.ConversationSettingsViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.sharedConversationModule
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.LocalKoinApplication
import org.koin.compose.LocalKoinScope
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatformTools
import org.koin.test.KoinTestRule
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
@Category(UnitTest::class)
internal class manage_conversations_moduleTest : BaseComposeTest() {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        androidContext(InstrumentationRegistry.getInstrumentation().context)

        modules(coreTestModules)
        modules(manageConversationsModule, sharedConversationModule)
    }

    private val courseId = 1L
    private val conversationId = 22L
    private val testContext = context.applicationContext as Application

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `the manage_conversations_module initializes ConversationOverviewViewModel without errors`() {
        composeTestRule.testViewModelInitialization<ConversationOverviewViewModel>() {
            parametersOf(testContext, courseId)
        }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `the manage_conversations_module initializes CreatePersonalConversationViewModel without errors`() {
        composeTestRule.testViewModelInitialization<CreatePersonalConversationViewModel>() {
            parametersOf(courseId)
        }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `the manage_conversations_module initializes CreateChannelViewModel without errors`() {
        composeTestRule.testViewModelInitialization<CreateChannelViewModel>() {
            parametersOf(courseId)
        }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `the manage_conversations_module initializes BrowseChannelsViewModel without errors`() {
        composeTestRule.testViewModelInitialization<BrowseChannelsViewModel>() {
            parametersOf(courseId)
        }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `the manage_conversations_module initializes ConversationSettingsViewModel without errors`() {
        composeTestRule.testViewModelInitialization<ConversationSettingsViewModel> {
            parametersOf(courseId, conversationId)
        }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `the manage_conversations_module initializes ConversationMembersViewModel without errors`() {
        composeTestRule.testViewModelInitialization<ConversationMembersViewModel> {
            parametersOf(courseId, conversationId)
        }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `the manage_conversations_module initializes ConversationAddMembersViewModel without errors`() {
        composeTestRule.testViewModelInitialization<ConversationAddMembersViewModel> {
            parametersOf(courseId, conversationId)
        }
    }

    /**
     * Test if a ViewModel can be initialized without errors.
     * @param parameters The parameters to pass to the ViewModel constructor
     */
    @OptIn(KoinInternalApi::class)
    private inline fun <reified T: ViewModel> ComposeContentTestRule.testViewModelInitialization(
        noinline parameters: ParametersDefinition? = null
    ) {
        setContent {
            // This is a workaround to make koin work in tests.
            // See: https://github.com/InsertKoinIO/koin/issues/1557#issue-1660665501
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                koinViewModel<T>(parameters = parameters)
            }
        }
    }
}