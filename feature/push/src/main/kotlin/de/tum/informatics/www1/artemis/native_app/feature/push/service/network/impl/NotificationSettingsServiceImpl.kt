package de.tum.informatics.www1.artemis.native_app.feature.push.service.network.impl

import android.util.Base64
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.feature.push.service.network.NotificationSettingsService
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.model.PushNotificationSetting
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

internal class NotificationSettingsServiceImpl(private val ktorProvider: KtorProvider) :
    NotificationSettingsService {

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
            }
                .body()
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

    override suspend fun uploadPushNotificationDeviceConfigurationsToServer(
        serverUrl: String,
        authToken: String,
        firebaseToken: String
    ): NetworkResponse<SecretKey> {
        return performNetworkCall {
            val response: RegisterResponseBody = ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments("api", "push_notification", "register")
                }

                cookieAuth(authToken)

                contentType(ContentType.Application.Json)
                setBody(RegisterRequestBody(token = firebaseToken))
            }.body()

            val keyBytes = Base64.decode(
                response.secretKey.toByteArray(Charsets.ISO_8859_1),
                Base64.DEFAULT
            )

            SecretKeySpec(keyBytes, "AES")
        }
    }

    override suspend fun unsubscribeFromNotifications(
        serverUrl: String,
        authToken: String,
        firebaseToken: String
    ): NetworkResponse<HttpStatusCode> {
        return performNetworkCall {
            ktorProvider.ktorClient.delete(serverUrl) {
                url {
                    appendPathSegments("api", "push_notification", "unregister")
                }

                cookieAuth(authToken)
                setBody(UnregisterRequestBody(firebaseToken))
                contentType(ContentType.Application.Json)
            }.status
        }
    }

    @Serializable
    private data class RegisterRequestBody(
        val token: String,
        val deviceType: String = "FIREBASE"
    )

    @Serializable
    private data class RegisterResponseBody(
        val secretKey: String,
        val algorithm: String
    )

    @Serializable
    private data class UnregisterRequestBody(
        val token: String,
        val deviceType: String = "FIREBASE"
    )
}
