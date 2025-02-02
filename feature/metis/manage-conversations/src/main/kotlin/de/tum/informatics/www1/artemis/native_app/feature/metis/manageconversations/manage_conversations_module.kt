package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations

import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.network.ChannelService
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.network.impl.ChannelServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.storage.ConversationPreferenceService
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.storage.impl.ConversationPreferenceStorageServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.browse_channels.BrowseChannelsViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_channel.CreateChannelViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_personal_conversation.CreatePersonalConversationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.ConversationOverviewViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.add_members.ConversationAddMembersViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.members.ConversationMembersViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.overview.ConversationSettingsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val manageConversationsModule = module {
    single<ConversationPreferenceService> {
        ConversationPreferenceStorageServiceImpl(
            androidContext()
        )
    }

    single<ChannelService> { ChannelServiceImpl(get()) }

    viewModel { params ->
        ConversationOverviewViewModel(
            androidApplication(),
            params.get(),
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
        CreatePersonalConversationViewModel(
            params.get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    viewModel { params ->
        CreateChannelViewModel(
            params.get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    viewModel { params ->
        BrowseChannelsViewModel(
            params.get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    viewModel { params ->
        ConversationSettingsViewModel(
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

    viewModel { params ->
        ConversationMembersViewModel(
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

    viewModel { params ->
        ConversationAddMembersViewModel(
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