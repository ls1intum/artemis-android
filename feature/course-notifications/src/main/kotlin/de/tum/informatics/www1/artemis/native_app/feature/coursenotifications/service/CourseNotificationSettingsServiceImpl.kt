package de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.service

import android.util.Log
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.model.NotificationSettings
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.model.NotificationSettingsInfo
import io.ktor.client.request.setBody
import io.ktor.http.appendPathSegments

class CourseNotificationSettingsServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
) : LoggedInBasedServiceImpl(ktorProvider, artemisContextProvider), CourseNotificationSettingsService {

    companion object {
        private const val TAG = "CourseNotificationSettingsServiceImpl"
    }

    override suspend fun getNotificationSettingsInfo(
        serverUrl: String,
        authToken: String
    ): NetworkResponse<NotificationSettingsInfo> {
        return getRequest {
            url {
                appendPathSegments(*Api.Communication.CourseNotifications.path, "info")
            }
            Log.d(TAG, "Fetching notification settings info from $url")
        }
    }

    override suspend fun getNotificationSettings(
        courseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<NotificationSettings> {
        return getRequest {
            url {
                appendPathSegments(*Api.Communication.CourseNotifications.path, courseId.toString(), "settings")
            }
            Log.d(TAG, "Fetching notification settings for course $courseId - URL: $url")

        }
    }

    override suspend fun updateSetting(
        courseId: Long,
        setting: NotificationSettings,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Unit> {
        return putRequest {
            url {
                appendPathSegments(*Api.Communication.CourseNotifications.path, courseId.toString(),"setting-specification" )
            }
            Log.d(TAG, "Updating setting for with value: $setting")
            setBody(setting)
            Log.d(TAG, "Updating notification setting for course $courseId and with: $setting - URL: $url")

        }
    }

    override suspend fun selectPreset(
        courseId: Long,
        presetId: Int,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Unit> {
        return putRequest {

            url {
                appendPathSegments(*Api.Communication.CourseNotifications.path, courseId.toString(), "setting-preset")
            }
            setBody(presetId)
            Log.d(TAG, "Selecting preset $presetId for course $courseId - URL: $url")

        }
    }
} 