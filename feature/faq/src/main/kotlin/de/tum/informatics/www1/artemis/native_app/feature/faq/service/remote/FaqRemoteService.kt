package de.tum.informatics.www1.artemis.native_app.feature.faq.service.remote

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.ArtemisContextBasedService

interface FaqRemoteService: ArtemisContextBasedService {

    suspend fun getFaqs(
        courseId: Long,
    ): NetworkResponse<List<FaqDto>>

    suspend fun getFaq(
        courseId: Long,
        faqId: Long,
    ): NetworkResponse<FaqDto>
}