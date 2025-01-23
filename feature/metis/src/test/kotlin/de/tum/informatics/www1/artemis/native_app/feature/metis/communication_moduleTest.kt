package de.tum.informatics.www1.artemis.native_app.feature.metis

import android.app.Application
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.work.WorkerParameters
import de.tum.informatics.www1.artemis.native_app.core.common.CurrentActivityListener
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.UserIdentifier
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostStatus
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.UserProfileDialogViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.SinglePageConversationBodyViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.user_conversation.NavigateToUserConversationViewModel
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
internal class communication_moduleTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun checkKoinModule() {
        communicationModule.verify(
            injections = listOf(
                definition<NavigateToUserConversationViewModel>(Long::class, UserIdentifier::class),
                definition<SinglePageConversationBodyViewModel>(Long::class),
                definition<UserProfileDialogViewModel>(Long::class, Long::class),
            ),
            extraTypes = listOf(
                ServerConfigurationService::class,
                NetworkStatusProvider::class,
                WebsocketProvider::class,
                AccountService::class,
                AccountDataService::class,
                CoroutineContext::class,
                ConversationService::class,
                CourseService::class,
                Long::class,
                Context::class,
                WorkerParameters::class,
                StandalonePostId::class,
                Application::class,
                CurrentActivityListener::class,
                SavedStateHandle::class,
                SavedPostStatus::class,
            )
        )
    }
}