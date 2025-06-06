package de.tum.informatics.www1.artemis.native_app.feature.faq.service.remote

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.CourseBasedService

interface FaqRemoteService: CourseBasedService {

    suspend fun getFaqs(): NetworkResponse<List<FaqDto>>

    suspend fun getFaq(
        faqId: Long,
    ): NetworkResponse<FaqDto>
}