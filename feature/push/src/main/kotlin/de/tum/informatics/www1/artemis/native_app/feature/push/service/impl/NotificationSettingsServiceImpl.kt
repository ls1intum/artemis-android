package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.model.PushNotificationSetting
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType

internal class NotificationSettingsServiceImpl(private val ktorProvider: KtorProvider) :
    de.tum.informatics.www1.artemis.native_app.feature.push.service.NotificationSettingsService {

    private companion object {
        private val pushNotificationSettingsResourcePathSegments = listOf("api", "notification-settings")
    }

    override suspend fun getNotificationSettings(
        serverUrl: String,
        authToken: String
    ): NetworkResponse<List<PushNotificationSetting>> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments(pushNotificationSettingsResourcePathSegments)
                }

                contentType(ContentType.Application.Json)
                cookieAuth(authToken)
            }.body()
        }
    }

    override suspend fun updateNotificationSettings(
        newSettings: List<PushNotificationSetting>,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<List<PushNotificationSetting>> {
        return performNetworkCall {
            ktorProvider.ktorClient.put(serverUrl) {
                url {
                    appendPathSegments(pushNotificationSettingsResourcePathSegments)
                }

                contentType(ContentType.Application.Json)
                cookieAuth(authToken)

                setBody(newSettings)
            }.body()
        }
    }
}