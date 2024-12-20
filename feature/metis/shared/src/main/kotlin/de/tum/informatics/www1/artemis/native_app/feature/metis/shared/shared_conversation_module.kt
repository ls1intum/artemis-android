package de.tum.informatics.www1.artemis.native_app.feature.metis.shared

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.impl.ConversationServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.UserProfileDialogViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val sharedConversationModule = module {
    single<ConversationService> {
        ConversationServiceImpl(
            get()
        )
    }

    viewModel { params ->
        UserProfileDialogViewModel(params[0], params[1], get(), get(), get(), get(), get(), get())
    }
}