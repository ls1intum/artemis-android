package de.tum.informatics.www1.artemis.native_app.feature.settings.service

import de.tum.informatics.www1.artemis.native_app.core.data.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.appendPathSegments
import io.ktor.utils.io.InternalAPI

// The backend ignores the file name, so we can use a placeholder here
private const val PLACEHOLDER_FILE_NAME = "profile-picture"

class ChangeProfilePictureServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider
): LoggedInBasedServiceImpl(ktorProvider, artemisContextProvider), ChangeProfilePictureService  {

    @OptIn(InternalAPI::class)
    override suspend fun upload(
        imageContentType: ContentType,
        fileBytes: ByteArray
    ): NetworkResponse<Account> {
        return putRequest {
            url {
                appendPathSegments(
                    *Api.Core.path,
                    "account",
                    "profile-picture",
                )
            }

            body = MultiPartFormDataContent(
                formData {
                    val fileName = "$PLACEHOLDER_FILE_NAME.${imageContentType.contentSubtype}"

                    append("file", fileBytes, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=$fileName")
                        append(HttpHeaders.ContentType, imageContentType.toString())
                    })
                }
            )
        }
    }

    override suspend fun delete(): NetworkResponse<Unit> {
        return deleteRequest {
            url {
                appendPathSegments(
                    *Api.Core.path,
                    "account",
                    "profile-picture",
                )
            }
        }
    }
}