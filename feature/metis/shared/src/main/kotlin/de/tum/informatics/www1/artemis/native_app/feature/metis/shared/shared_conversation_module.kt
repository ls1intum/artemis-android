package de.tum.informatics.www1.artemis.native_app.feature.metis.shared

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.SavedPostService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.impl.ConversationServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.impl.SavedPostServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.UserProfileDialogViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val sharedConversationModule = module {
    single<ConversationService> { ConversationServiceImpl(get()) }
    single<SavedPostService> { SavedPostServiceImpl(get()) }

    viewModel { params ->
        UserProfileDialogViewModel(params[0], params[1], get(), get(), get(), get(), get(), get())
    }
}