package de.tum.informatics.www1.artemis.native_app.feature.metis

import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.conversationModule
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.manageConversationsModule
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.CodeOfConductService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.CodeOfConductStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.impl.CodeOfConductServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.impl.CodeOfConductStorageServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.sharedConversationModule
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.CodeOfConductViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val communicationModule = module {
    includes(sharedConversationModule, conversationModule, manageConversationsModule)

    single<CodeOfConductService> { CodeOfConductServiceImpl(get()) }
    single<CodeOfConductStorageService> { CodeOfConductStorageServiceImpl(androidContext()) }

    viewModel { params -> CodeOfConductViewModel(params.get(), get(), get(), get(), get(), get(), get()) }
}