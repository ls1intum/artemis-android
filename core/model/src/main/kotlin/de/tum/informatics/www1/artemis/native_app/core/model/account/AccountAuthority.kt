package de.tum.informatics.www1.artemis.native_app.core.model.account

import kotlinx.serialization.Serializable

@Serializable
enum class AccountAuthority : Comparable<AccountAuthority> {
    ROLE_USER,
    /** Teaching assistant */
    ROLE_TA,
    ROLE_EDITOR,
    ROLE_INSTRUCTOR,
    ROLE_ADMIN
}