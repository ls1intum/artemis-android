package de.tum.informatics.www1.artemis.native_app.feature.login

import androidx.lifecycle.SavedStateHandle
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerProfileInfoService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.login.saml2_login.Saml2LoginViewModel
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
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
internal class login_moduleTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun checkKoinModule() {
        loginModule.verify(
            injections = listOf(
                definition<Saml2LoginViewModel>(Boolean::class)     // rememberMe
            ),
            extraTypes = listOf(
                ServerConfigurationService::class,
                NetworkStatusProvider::class,
                SavedStateHandle::class,
                PushNotificationConfigurationService::class,
                ServerProfileInfoService::class,
                AccountService::class,
                CoroutineContext::class,
            )
        )
    }
}