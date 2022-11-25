package de.tum.informatics.www1.artemis.native_app.core.communication

enum class MetisFilter {
    /**
     * Filter for the courses this user has created.
     */
    CREATED_BY_CLIENT,
    /**
     * Filter for posts which already have a reaction.
     */
    WITH_REACTION,
    /**
     * Filter for courses which already have been resolved
     */
    RESOLVED
}