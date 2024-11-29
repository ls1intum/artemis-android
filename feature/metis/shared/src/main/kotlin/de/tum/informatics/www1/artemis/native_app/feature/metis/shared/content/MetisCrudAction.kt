package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content

import kotlinx.serialization.Serializable

@Serializable
enum class MetisCrudAction(val value: String) {
    CREATE("CREATE"),
    UPDATE("UPDATE"),
    DELETE("DELETE"),
    NEW_MESSAGE("NEW_MESSAGE")    // Only used when for the first message in a new conversation.
}