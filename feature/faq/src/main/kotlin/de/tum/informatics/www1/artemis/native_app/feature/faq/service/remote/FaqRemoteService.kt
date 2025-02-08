package de.tum.informatics.www1.artemis.native_app.feature.faq.service.remote

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse

interface FaqRemoteService {

    suspend fun getFaqs(
        courseId: Long,
        authToken: String,
        serverUrl: String,
    ): NetworkResponse<List<FaqDto>>
}