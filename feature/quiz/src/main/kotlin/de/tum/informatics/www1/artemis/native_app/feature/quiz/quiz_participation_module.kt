package de.tum.informatics.www1.artemis.native_app.feature.quiz

import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizParticipationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.quiz.service.QuizExerciseService
import de.tum.informatics.www1.artemis.native_app.feature.quiz.service.QuizParticipationService
import de.tum.informatics.www1.artemis.native_app.feature.quiz.service.impl.QuizExerciseServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.quiz.service.impl.QuizParticipationServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result.QuizResultViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val quizParticipationModule = module {
    single<QuizExerciseService> { QuizExerciseServiceImpl(get()) }
    single<QuizParticipationService> { QuizParticipationServiceImpl(get()) }

    viewModel { params ->
        QuizParticipationViewModel(
            params[0],
            params[1],
            params[2],
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    viewModel { params ->
        QuizResultViewModel(
            params[0],
            params[1],
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}