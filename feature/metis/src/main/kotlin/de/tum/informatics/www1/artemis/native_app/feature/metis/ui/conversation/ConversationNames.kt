package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation

import de.tum.informatics.www1.artemis.native_app.core.model.account.BaseAccount
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.conversation.OneToOneChat

val GroupChat.humanReadableTitle: String
    get() {
        return name ?: members.joinToString { it.humanReadableName }
    }

val OneToOneChat.humanReadableTitle: String
    get() {
        return members.filterNot { it.isRequestingUser }.firstOrNull()?.humanReadableName.orEmpty()
    }

val BaseAccount.humanReadableName: String
    get() {
        return "$firstName $lastName"
    }

