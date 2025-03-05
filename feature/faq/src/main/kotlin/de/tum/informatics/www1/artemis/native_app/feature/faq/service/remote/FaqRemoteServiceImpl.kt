package de.tum.informatics.www1.artemis.native_app.feature.faq.service.remote

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.appendPathSegments

class FaqRemoteServiceImpl(
    private val ktorProvider: KtorProvider,
) : FaqRemoteService {

    override suspend fun getFaqs(
        courseId: Long,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<List<FaqDto>> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments(
                        "api",
                        "courses",
                        courseId.toString(),
                        "faq-state",
                        "ACCEPTED"          // For now we only want to show accepted FAQs
                    )
                }

                cookieAuth(authToken)
            }.body()
        }
    }

    override suspend fun getFaq(
        courseId: Long,
        faqId: Long,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<FaqDto> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments(
                        "api",
                        "courses",
                        courseId.toString(),
                        "faqs",
                        faqId.toString()
                    )
                }

                cookieAuth(authToken)
            }.body()
        }
    }
}