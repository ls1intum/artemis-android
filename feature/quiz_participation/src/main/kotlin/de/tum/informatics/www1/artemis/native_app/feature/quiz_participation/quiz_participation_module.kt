package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation

import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.service.QuizExerciseService
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.service.impl.QuizExerciseServiceImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val quizParticipationModule = module {
    single<QuizExerciseService> { QuizExerciseServiceImpl(get()) }

    viewModel { params -> QuizParticipationViewModel(params[0], params[1], get(), get()) }
}