package de.tum.informatics.www1.artemis.native_app.feature.faq.repository

import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.Faq
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.mappers.toFaq
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.remote.FaqRemoteService
import kotlinx.coroutines.flow.Flow

class FaqRepositoryImpl(
    private val remoteService: FaqRemoteService,
    private val networkStatusProvider: NetworkStatusProvider,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
) : FaqRepository {

    override suspend fun getFaqs(
        courseId: Long,
    ): Flow<DataState<List<Faq>>> {
        return flatMapLatest(
            accountService.authToken,
            serverConfigurationService.serverUrl,
        ) { authToken, serverUrl ->
            retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                remoteService.getFaqs(
                    courseId = courseId,
                    authToken = authToken,
                    serverUrl = serverUrl,
                ).bind {
                    it.map { faqDto -> faqDto.toFaq() }
                }
            }
        }
    }

    override suspend fun getFaq(
        courseId: Long,
        faqId: Long,
    ): Flow<DataState<Faq>> {
        return flatMapLatest(
            accountService.authToken,
            serverConfigurationService.serverUrl,
        ) { authToken, serverUrl ->
            retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                remoteService.getFaq(
                    courseId = courseId,
                    faqId = faqId,
                    authToken = authToken,
                    serverUrl = serverUrl,
                ).bind {
                    it.toFaq()
                }
            }
        }
    }
}