package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation

import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.EmojiService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.impl.EmojiServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisContextManager
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.impl.MetisModificationServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.impl.MetisServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.impl.MetisStorageServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.MetisListViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread.MetisThreadViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val conversationModule = module {
    single<MetisService> { MetisServiceImpl(get(), get()) }
    single<MetisModificationService> { MetisModificationServiceImpl(get()) }
    single<EmojiService> { EmojiServiceImpl(androidContext()) }

    single<MetisStorageService> { MetisStorageServiceImpl(get()) }

    single<MetisContextManager> { MetisContextManager(get(), get()) }

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
}