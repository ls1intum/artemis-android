package de.tum.informatics.www1.artemis.native_app.feature.metis.shared

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.impl.ConversationServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.UserProfileDialogViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val sharedConversationModule = module {
    single<ConversationService> { ConversationServiceImpl(get()) }

    viewModel { params ->
        UserProfileDialogViewModel(
            courseId = params[0],
            userId = params[1],
            accountDataService = get(),
            networkStatusProvider = get(),
        )
    }
}