package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content

import kotlinx.serialization.Serializable

@Serializable
enum class MetisCrudAction(val value: String) {
    CREATE("CREATE"),
    UPDATE("UPDATE"),
    DELETE("DELETE"),
}