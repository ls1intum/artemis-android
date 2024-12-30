package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation

import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.CreatePostService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.EmojiService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.impl.CreatePostServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.impl.EmojiServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.impl.MetisModificationServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.impl.MetisServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.ReplyTextStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.impl.MetisStorageServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.impl.ReplyTextStorageServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.ConversationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.work.CreateClientSidePostWorker
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.work.SendConversationPostWorker
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val conversationModule = module {
    single<MetisService> { MetisServiceImpl(get()) }
    single<MetisModificationService> { MetisModificationServiceImpl(get()) }
    single<EmojiService> { EmojiServiceImpl(androidContext()) }

    single<MetisStorageService> { MetisStorageServiceImpl(get()) }
    single<ReplyTextStorageService> { ReplyTextStorageServiceImpl(androidContext()) }

    single<CreatePostService> { CreatePostServiceImpl(androidContext()) }

    workerOf(::SendConversationPostWorker)
    workerOf(::CreateClientSidePostWorker)

    viewModel { params ->
        ConversationViewModel(
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
            get(),
            get(),
            get(),
            get()
        )
    }
}