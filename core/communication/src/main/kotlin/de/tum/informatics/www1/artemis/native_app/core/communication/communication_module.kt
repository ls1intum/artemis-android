package de.tum.informatics.www1.artemis.native_app.core.communication

import de.tum.informatics.www1.artemis.native_app.core.communication.emoji.EmojiService
import de.tum.informatics.www1.artemis.native_app.core.communication.impl.EmojiServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.communication.impl.MetisContextManager
import de.tum.informatics.www1.artemis.native_app.core.communication.impl.MetisServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.list.MetisListViewModel
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.standalone_post.MetisStandalonePostViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val communicationModule = module {
    single<MetisService> { MetisServiceImpl(get(), get()) }
    single<EmojiService> { EmojiServiceImpl(androidContext()) }

    viewModel { params ->
        MetisListViewModel(
            metisContext = params.get(),
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