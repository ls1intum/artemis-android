package de.tum.informatics.www1.artemis.native_app.feature.faq.repository

import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.Faq
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.mappers.toFaq
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.remote.FaqRemoteService
import kotlinx.coroutines.flow.Flow

class FaqRepositoryImpl(
    private val remoteService: FaqRemoteService,
    private val networkStatusProvider: NetworkStatusProvider,
) : FaqRepository {

    override suspend fun getFaqs(
        courseId: Long,
        authToken: String,
        serverUrl: String,
    ): Flow<DataState<List<Faq>>> {
        return retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            remoteService.getFaqs(
                courseId = courseId,
                authToken = authToken,
                serverUrl = serverUrl,
            ).bind {
                it.map { faqDto -> faqDto.toFaq() }
            }
        }
    }

    override suspend fun getFaq(
        courseId: Long,
        faqId: Long,
        authToken: String,
        serverUrl: String,
    ): Flow<DataState<Faq>> {
        return retryOnInternet(networkStatusProvider.currentNetworkStatus) {
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