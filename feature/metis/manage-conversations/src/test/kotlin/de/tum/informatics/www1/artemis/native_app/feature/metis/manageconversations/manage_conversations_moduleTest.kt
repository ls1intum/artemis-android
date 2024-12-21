package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import de.tum.informatics.www1.artemis.native_app.core.common.CurrentActivityListener
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.storage.ConversationPreferenceService
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.browse_channels.BrowseChannelsViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_channel.CreateChannelViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_personal_conversation.CreatePersonalConversationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.ConversationOverviewViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.add_members.ConversationAddMembersViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.members.ConversationMembersViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.overview.ConversationSettingsViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.definition
import org.koin.test.verify.verify
import org.robolectric.RobolectricTestRunner
import kotlin.coroutines.CoroutineContext


@RunWith(RobolectricTestRunner::class)
@Category(UnitTest::class)
internal class manage_conversations_moduleTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun checkKoinModule() {
        manageConversationsModule.verify(
            injections = listOf(
                definition<ConversationOverviewViewModel>(Application::class, String::class),
                definition<CreatePersonalConversationViewModel>(Long::class),
                definition<CreateChannelViewModel>(Long::class),
                definition<BrowseChannelsViewModel>(Long::class),
                definition<ConversationSettingsViewModel>(Long::class, Long::class),
                definition<ConversationMembersViewModel>(Long::class, Long::class),
                definition<ConversationAddMembersViewModel>(Long::class, Long::class),
            ),
            extraTypes = listOf(
                ServerConfigurationService::class,
                NetworkStatusProvider::class,
                WebsocketProvider::class,
                AccountService::class,
                AccountDataService::class,
                ConversationPreferenceService::class,
                ConversationService::class,
                CourseService::class,
                CoroutineContext::class,
                CurrentActivityListener::class,
                SavedStateHandle::class,
            )
        )
    }
}