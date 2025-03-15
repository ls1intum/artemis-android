package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.saved_posts

import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.saved_posts.service.SavedPostService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.saved_posts.service.SavedPostServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.saved_posts.ui.SavedPostsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val saved_posts_module = module {
    single<SavedPostService> { SavedPostServiceImpl(get()) }

    viewModel { params ->
        SavedPostsViewModel(
            params[0],
            params[1],
            get(),
            get(),
            get(),
            get()
        )
    }
}