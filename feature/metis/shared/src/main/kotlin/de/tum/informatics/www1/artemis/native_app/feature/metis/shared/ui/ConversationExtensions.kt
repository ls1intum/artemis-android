package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui

import de.tum.informatics.www1.artemis.native_app.core.model.account.BaseAccount
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat

val Conversation.humanReadableName: String
    get() = when(this) {
        is ChannelChat -> name
        is GroupChat -> humanReadableTitle
        is OneToOneChat -> humanReadableTitle
    }

val GroupChat.humanReadableTitle: String
    get() {
        return name ?: members.joinToString { it.humanReadableName }
    }

val OneToOneChat.humanReadableTitle: String
    get() {
        return partner.humanReadableName
    }

val BaseAccount.humanReadableName: String
    get() {
        return name ?: "$firstName $lastName"
    }
