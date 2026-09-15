package de.tum.informatics.www1.artemis.native_app.core.data.service.network.impl

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.ParticipationService
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import io.ktor.http.appendPathSegments

internal class ParticipationServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
) : LoggedInBasedServiceImpl(ktorProvider, artemisContextProvider),  ParticipationService {
    override suspend fun findParticipation(exerciseId: Long): NetworkResponse<Participation> {
        // Artemis 8.6.1 removed "exercise/exercises/{exerciseId}/participation"; the quiz module now
        // owns the only variant the app used, and it starts the participation when there is none yet.
        return postRequest {
            url {
                appendPathSegments(
                    *Api.Quiz.QuizExercises.path,
                    exerciseId.toString(),
                    "start-participation"
                )
            }
        }
    }
}
