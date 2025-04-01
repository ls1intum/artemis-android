package de.tum.informatics.www1.artemis.native_app.feature.faq.repository

import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.performAutoReloadingNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.Faq
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.mappers.toFaq
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.remote.FaqRemoteService
import kotlinx.coroutines.flow.Flow

class FaqRepositoryImpl(
    private val remoteService: FaqRemoteService,
    private val networkStatusProvider: NetworkStatusProvider,
) : FaqRepository {

    override fun getFaqs(
    ): Flow<DataState<List<Faq>>> {
        return remoteService.performAutoReloadingNetworkCall(networkStatusProvider) {
            remoteService.getFaqs().bind {
                it.map { faqDto -> faqDto.toFaq() }
            }
        }
    }

    override fun getFaq(
        faqId: Long,
    ): Flow<DataState<Faq>> {
        return remoteService.performAutoReloadingNetworkCall(networkStatusProvider) {
            remoteService.getFaq(
                faqId = faqId,
            ).bind {
                it.toFaq()
            }
        }
    }
}