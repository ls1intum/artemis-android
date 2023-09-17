package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.content

import kotlinx.serialization.Serializable

@Serializable
enum class MetisPostAction(val value: String) {
    CREATE("CREATE"),
    UPDATE("UPDATE"),
    DELETE("DELETE"),
    NEW_MESSAGE("NEW_MESSAGE")
}