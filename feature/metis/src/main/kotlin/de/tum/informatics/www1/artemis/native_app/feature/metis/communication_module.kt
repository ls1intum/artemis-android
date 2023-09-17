package de.tum.informatics.www1.artemis.native_app.feature.metis

import de.tum.informatics.www1.artemis.native_app.feature.metis.service.network.ChannelService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.storage.ConversationPreferenceService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.EmojiService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.network.impl.ChannelServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.storage.impl.ConversationPreferenceStorageServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.impl.ConversationServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.impl.EmojiServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisContextManager
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.impl.MetisModificationServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.impl.MetisServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.impl.MetisStorageServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.browse_channels.BrowseChannelsViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_channel.CreateChannelViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_personal_conversation.CreatePersonalConversationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.overview.ConversationOverviewViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.add_members.ConversationAddMembersViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.members.ConversationMembersViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.overview.ConversationSettingsViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.MetisListViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread.MetisThreadViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val communicationModule = module {
    single<ConversationPreferenceService> { ConversationPreferenceStorageServiceImpl(androidContext()) }

    single<MetisService> { MetisServiceImpl(get(), get()) }
    single<de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService> {
        de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.impl.ConversationServiceImpl(
            get()
        )
    }
    single<ChannelService> { ChannelServiceImpl(get()) }
    single<MetisModificationService> { MetisModificationServiceImpl(get()) }
    single<EmojiService> { EmojiServiceImpl(androidContext()) }

    single<MetisStorageService> { MetisStorageServiceImpl(get()) }

    viewModel { params ->
        MetisListViewModel(
            initialMetisContext = params.get(),
            get(),
            get(),
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
        MetisThreadViewModel(
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
            get(),
            get(),
            get()
        )
    }

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