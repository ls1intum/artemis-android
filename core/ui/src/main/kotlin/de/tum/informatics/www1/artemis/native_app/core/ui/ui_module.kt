package de.tum.informatics.www1.artemis.native_app.core.ui

import de.tum.informatics.www1.artemis.native_app.core.ui.common.image.RetryableAsyncImageViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val uiModule = module {
    viewModel { params -> RetryableAsyncImageViewModel(androidContext(), params.get()) }
}