package de.tum.informatics.www1.artemis.native_app.feature.settings.service

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.ArtemisContextBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.appendPathSegments
import io.ktor.utils.io.InternalAPI

class ChangeProfilePictureServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider
): ArtemisContextBasedServiceImpl(ktorProvider, artemisContextProvider), ChangeProfilePictureService  {

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
                    append("file", fileBytes, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=profile-picture.jpg")
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