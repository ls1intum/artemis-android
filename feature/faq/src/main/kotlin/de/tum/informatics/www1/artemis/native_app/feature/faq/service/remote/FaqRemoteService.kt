package de.tum.informatics.www1.artemis.native_app.feature.faq.service.remote

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedService

interface FaqRemoteService: LoggedInBasedService {

    suspend fun getFaqs(
        courseId: Long,
    ): NetworkResponse<List<FaqDto>>

    suspend fun getFaq(
        courseId: Long,
        faqId: Long,
    ): NetworkResponse<FaqDto>
}