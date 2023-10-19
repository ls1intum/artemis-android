package de.tum.informatics.www1.artemis.native_app.feature.metis

import de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.cocModule
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.conversationModule
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.manageConversationsModule
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.sharedConversationModule
import org.koin.dsl.module

val communicationModule = module {
    includes(sharedConversationModule, conversationModule, manageConversationsModule, cocModule)
}
