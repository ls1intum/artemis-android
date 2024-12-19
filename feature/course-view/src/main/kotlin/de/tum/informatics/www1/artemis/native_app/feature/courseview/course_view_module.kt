package de.tum.informatics.www1.artemis.native_app.feature.courseview

import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.CourseViewModel
import org.koin.core.module.dsl.viewModel
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
