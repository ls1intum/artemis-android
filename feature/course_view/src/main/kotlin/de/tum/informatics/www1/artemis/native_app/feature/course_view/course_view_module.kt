package de.tum.informatics.www1.artemis.native_app.feature.course_view

import de.tum.informatics.www1.artemis.native_app.feature.course_view.ui.CourseViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val courseViewModule = module {
    viewModel {
        CourseViewModel(
            it.get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}
