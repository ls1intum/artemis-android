package de.tum.informatics.www1.artemis.native_app.feature.faq.service.remote

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedServiceImpl
import io.ktor.http.appendPathSegments

class FaqRemoteServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
): LoggedInBasedServiceImpl(ktorProvider, artemisContextProvider), FaqRemoteService {

    override suspend fun getFaqs(
        courseId: Long,
    ): NetworkResponse<List<FaqDto>> {
        return getRequest {
            url {
                appendPathSegments(
                    *Api.Communication.Courses.path,
                    courseId.toString(),
                    "faq-state",
                    "ACCEPTED"          // For now we only want to show accepted FAQs
                )
            }
        }
    }

    override suspend fun getFaq(
        courseId: Long,
        faqId: Long,
    ): NetworkResponse<FaqDto> {
        return getRequest {
            url {
                appendPathSegments(
                    *Api.Communication.Courses.path,
                    courseId.toString(),
                    "faqs",
                    faqId.toString()
                )
            }
        }
    }
}