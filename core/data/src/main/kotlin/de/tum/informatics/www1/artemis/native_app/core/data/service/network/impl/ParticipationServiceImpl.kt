package de.tum.informatics.www1.artemis.native_app.core.data.service.network.impl

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.ArtemisContextBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.ParticipationService
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import io.ktor.http.appendPathSegments

internal class ParticipationServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
) : ArtemisContextBasedServiceImpl(ktorProvider, artemisContextProvider),  ParticipationService {
    override suspend fun findParticipation(exerciseId: Long): NetworkResponse<Participation> {
        return getRequest {
            url {
                appendPathSegments(*Api.Exercise.Exercises.path, exerciseId.toString(), "participation")
            }
        }
    }
}