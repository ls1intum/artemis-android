package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation

import de.tum.informatics.www1.artemis.native_app.core.model.account.BaseAccount
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ConversationUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.OneToOneChat

val GroupChat.humanReadableTitle: String
    get() {
        return name ?: members.joinToString { it.humanReadableName }
    }

val OneToOneChat.humanReadableTitle: String
    get() {
        return members.firstOrNull()?.humanReadableName.orEmpty()
    }

val BaseAccount.humanReadableName: String
    get() {
        return "$firstName $lastName"
    }

