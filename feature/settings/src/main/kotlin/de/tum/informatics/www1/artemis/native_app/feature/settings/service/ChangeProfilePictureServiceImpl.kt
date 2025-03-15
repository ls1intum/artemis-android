package de.tum.informatics.www1.artemis.native_app.feature.settings.service

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.ArtemisContextBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.appendPathSegments

class ChangeProfilePictureServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider
): ArtemisContextBasedServiceImpl(ktorProvider, artemisContextProvider), ChangeProfilePictureService  {

    override suspend fun upload(fileBytes: ByteArray): NetworkResponse<Account> {
        return performNetworkCall {
            ktorProvider.ktorClient.submitFormWithBinaryData(
                formData = formData {
                    append("file", fileBytes)
                }
            ) {
                url {
                    appendPathSegments(
                        *Api.Core.path,
                        "account",
                        "profile-picture",
                    )
                }

                cookieAuth(authToken())
            }.body()
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