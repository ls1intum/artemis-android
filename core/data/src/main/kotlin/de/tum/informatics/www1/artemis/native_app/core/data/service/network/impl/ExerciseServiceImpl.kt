package de.tum.informatics.www1.artemis.native_app.core.data.service.network.impl

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.ExerciseService
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import io.ktor.http.appendPathSegments
import kotlinx.serialization.Serializable

internal class ExerciseServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
) : LoggedInBasedServiceImpl(ktorProvider, artemisContextProvider), ExerciseService {

    // For some reason the API endpoint does not return an exercise directly, but this wrapper.
    @Serializable
    private data class ExerciseWrapper(
        val exercise: Exercise
    )


    override suspend fun getExerciseDetails(
        exerciseId: Long,
    ): NetworkResponse<Exercise> {
        return getRequest<ExerciseWrapper> {
            url {
                appendPathSegments(*Api.Exercise.Exercises.path, exerciseId.toString(), "details")
            }
        }.bind { it.exercise }
    }
}
