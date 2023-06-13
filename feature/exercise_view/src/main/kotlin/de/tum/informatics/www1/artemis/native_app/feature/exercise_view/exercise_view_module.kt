package de.tum.informatics.www1.artemis.native_app.feature.exercise_view

import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.participate.text_exercise.TextExerciseParticipationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.service.TextEditorService
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.service.TextSubmissionService
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.service.impl.TextEditorServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.service.impl.TextSubmissionServiceImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val exerciseViewModule = module {
    viewModel { parametersHolder ->
        ExerciseViewModel(
            parametersHolder.get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    viewModel { params ->
        TextExerciseParticipationViewModel(params[0], params[1], get(), get(), get(), get(), get())
    }

    single<TextSubmissionService> { TextSubmissionServiceImpl(get()) }
    single<TextEditorService> { TextEditorServiceImpl(get()) }
}