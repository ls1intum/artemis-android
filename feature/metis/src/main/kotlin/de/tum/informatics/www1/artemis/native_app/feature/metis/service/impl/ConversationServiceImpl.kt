package de.tum.informatics.www1.artemis.native_app.feature.metis.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.ConversationService
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.appendPathSegments

class ConversationServiceImpl(private val ktorProvider: KtorProvider) : ConversationService {

    override suspend fun getConversations(
        courseId: Long,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<List<Conversation>> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments("api", "courses", courseId.toString(), "conversations")
                }

                cookieAuth(authToken)
            }.body()
        }
    }

    override suspend fun searchForPotentialCommunicationParticipants(
        courseId: Long,
        query: String,
        includeStudents: Boolean,
        includeTutors: Boolean,
        includeInstructors: Boolean,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<List<User>> {
        val roles: List<String> =
            (if (includeStudents) listOf("students") else emptyList()) +
                    (if (includeInstructors) listOf("instructors") else emptyList()) +
                    (if (includeTutors) listOf("tutors") else emptyList())

        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments("api", "courses", courseId.toString(), "users", "search")

                    parameter("loginOrName", query)
                    parameter("roles", roles.joinToString(","))
                }

                cookieAuth(authToken)
            }.body()
        }
    }
}