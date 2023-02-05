package de.tum.informatics.www1.artemis.native_app.feature.metis

import de.tum.informatics.www1.artemis.native_app.feature.metis.emoji.EmojiService
import de.tum.informatics.www1.artemis.native_app.feature.metis.impl.EmojiServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.impl.MetisContextManager
import de.tum.informatics.www1.artemis.native_app.feature.metis.impl.MetisServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.create_standalone_post.CreateStandalonePostViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.list.MetisListViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post.MetisStandalonePostViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val communicationModule = module {
    single<MetisService> { MetisServiceImpl(get(), get()) }
    single<EmojiService> {
        EmojiServiceImpl(
            androidContext()
        )
    }

    viewModel { params ->
        MetisListViewModel(
            metisContext = params.get(),
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
        MetisStandalonePostViewModel(
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
            get()
        )
    }

    viewModel { params ->
        CreateStandalonePostViewModel(
            params.get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    singleOf(::MetisContextManager)
}