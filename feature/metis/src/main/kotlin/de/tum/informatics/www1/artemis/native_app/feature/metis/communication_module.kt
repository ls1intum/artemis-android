package de.tum.informatics.www1.artemis.native_app.feature.metis

import de.tum.informatics.www1.artemis.native_app.feature.metis.service.ChannelService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.EmojiService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.impl.EmojiServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.impl.MetisContextManager
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.impl.MetisModificationServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.impl.MetisServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.impl.MetisStorageServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.impl.ChannelServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.impl.ConversationServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.browse_channels.BrowseChannelsViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_channel.CreateChannelViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_personal_conversation.CreatePersonalConversationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.overview.ConversationOverviewViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.qna.create_standalone_post.CreateStandalonePostViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list.MetisListViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post.MetisStandalonePostViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val communicationModule = module {
    single<MetisService> { MetisServiceImpl(get(), get()) }
    single<ConversationService> { ConversationServiceImpl(get()) }
    single<ChannelService> { ChannelServiceImpl(get()) }
    single<MetisModificationService> { MetisModificationServiceImpl(get()) }
    single<EmojiService> { EmojiServiceImpl(androidContext()) }

    single<MetisStorageService> { MetisStorageServiceImpl(get()) }

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
            get(),
            get()
        )
    }

    viewModel { params ->
        ConversationOverviewViewModel(
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
        CreatePersonalConversationViewModel(
            params.get(),
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

    singleOf(::MetisContextManager)
}