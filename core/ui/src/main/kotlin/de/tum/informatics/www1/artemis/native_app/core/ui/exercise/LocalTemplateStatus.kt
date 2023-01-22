package de.tum.informatics.www1.artemis.native_app.core.ui.exercise

import androidx.compose.runtime.*
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.websocket.LiveParticipationService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.shareIn
import org.koin.androidx.compose.get

val LocalTemplateStatusProvider: ProvidableCompositionLocal<@Composable () -> ResultTemplateStatus?> =
    compositionLocalOf { { throw RuntimeException("No template status provider set") } }

/**
 * Computing the template status is an expensive operation, therefore it should be shared among composables
 * that require it. For this purpose a local composition provider is used to simply the sharing logic.
 */
@Composable
fun ProvideDefaultExerciseTemplateStatus(exercise: Exercise, content: @Composable () -> Unit) {
    val scope = rememberCoroutineScope()
    val liveParticipationService: LiveParticipationService = get()

    val templateStatusFlow = remember(exercise) {
        computeTemplateStatus(
            service = liveParticipationService,
            exercise = exercise,
            participation = exercise.studentParticipations.orEmpty().firstOrNull() ?: return@remember emptyFlow(),
            result = null,
            showUngradedResults = true,
            personal = true
        )
            .shareIn(scope, SharingStarted.Lazily, replay = 1)
    }
    val provider = @Composable {
        templateStatusFlow.collectAsState(initial = ResultTemplateStatus.NoResult).value
    }

    CompositionLocalProvider(LocalTemplateStatusProvider provides provider, content = content)
}