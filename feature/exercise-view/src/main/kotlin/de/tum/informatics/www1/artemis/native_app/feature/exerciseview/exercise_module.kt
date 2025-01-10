package de.tum.informatics.www1.artemis.native_app.feature.exerciseview

import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise.TextExerciseParticipationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service.TextEditorService
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service.TextSubmissionService
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service.impl.TextEditorServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service.impl.TextSubmissionServiceImpl
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val exerciseModule = module {
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