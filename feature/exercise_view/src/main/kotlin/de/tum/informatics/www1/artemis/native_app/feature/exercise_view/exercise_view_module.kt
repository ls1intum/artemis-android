package de.tum.informatics.www1.artemis.native_app.feature.exercise_view

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
}