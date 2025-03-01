package de.tum.informatics.www1.artemis.native_app.core.data.service.network.impl

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.ApiEndpoint
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.ArtemisContextBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseExerciseService
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import io.ktor.http.appendPathSegments

internal class CourseExerciseServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
) : ArtemisContextBasedServiceImpl(ktorProvider, artemisContextProvider), CourseExerciseService {

    override suspend fun startExercise(
        exerciseId: Long,
    ): NetworkResponse<Participation> {
        return postRequest {
            url {
                appendPathSegments(*ApiEndpoint.exercise_exercises, exerciseId.toString(), "participations")
            }
        }
    }
}