package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content

sealed class MetisFilter {
    /**
     * Disabled filter.
     */
    data object All : MetisFilter()

    /**
     * Filter for the messages this user has created.
     * @param userId The ID of the user who created the posts
     */
    data class CreatedByClient(val userId: Long) : MetisFilter()

    /**
     * Filter for the messages this user has created.
     * @param userId The ID of the user who created the posts
     */
    data class CreatedByAuthors(val userIds: List<Long>) : MetisFilter()

    /**
     * Filter for posts which already have a reaction.
     */
    data object WithReaction : MetisFilter()

    /**
     * Filter for posts which have not been resolved yet
     */
    data object Unresolved : MetisFilter()

    /**
     * Filter for posts which are pinned
     */
    data object Pinned : MetisFilter()
}