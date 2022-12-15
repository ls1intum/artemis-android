package de.tum.informatics.www1.artemis.native_app.feature.exercise_view

import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.participate.text_exercise.TextExerciseParticipationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.service.TextSubmissionService
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.service.impl.TextSubmissionServiceImpl
import org.koin.android.ext.koin.androidContext
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
            get(),
            get(),
            androidContext()
        )
    }

    viewModel { params ->
        TextExerciseParticipationViewModel(params.get())
    }

    single<TextSubmissionService> { TextSubmissionServiceImpl(get()) }
}