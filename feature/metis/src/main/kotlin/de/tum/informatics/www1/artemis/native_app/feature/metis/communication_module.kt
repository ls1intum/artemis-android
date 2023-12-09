package de.tum.informatics.www1.artemis.native_app.feature.metis

import de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.cocModule
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.conversationModule
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.manageConversationsModule
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.sharedConversationModule
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.NavigateToUserConversationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val communicationModule = module {
    includes(sharedConversationModule, conversationModule, manageConversationsModule, cocModule)

    viewModel<NavigateToUserConversationViewModel> { params ->
        NavigateToUserConversationViewModel(
            params[0],
            params[1],
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}
