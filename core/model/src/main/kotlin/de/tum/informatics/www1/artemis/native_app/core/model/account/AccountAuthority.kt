package de.tum.informatics.www1.artemis.native_app.core.model.account

import kotlinx.serialization.Serializable

/**
 * Ordered from least to most privileged, matching the server's own precedence
 * (SUPER_ADMIN > ADMIN > INSTRUCTOR > EDITOR > TA > STUDENT): the ordering is what [Comparable]
 * compares on.
 */
@Serializable
enum class AccountAuthority : Comparable<AccountAuthority> {
    ROLE_USER,
    /** Teaching assistant */
    ROLE_TA,
    ROLE_EDITOR,
    ROLE_INSTRUCTOR,
    ROLE_ADMIN,

    /**
     * Held by the internal administrator instead of [ROLE_ADMIN], so an account response that omits
     * it cannot be decoded at all: an unknown name inside a list of enums throws rather than being
     * coerced to a default.
     */
    ROLE_SUPER_ADMIN
}