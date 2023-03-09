package de.tum.informatics.www1.artemis.native_app.feature.metis

import kotlinx.serialization.Serializable

@Serializable
enum class MetisPostAction(val value: String) {
    CREATE("CREATE"),
    UPDATE("UPDATE"),
    DELETE("DELETE"),
    READ_CONVERSATION("READ_CONVERSATION")
}