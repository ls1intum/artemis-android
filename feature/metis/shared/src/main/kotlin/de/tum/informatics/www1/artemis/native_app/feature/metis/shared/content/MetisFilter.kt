package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content

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
     * Filter for posts which have not been resolved yet
     */
    UNRESOLVED
}