package de.tum.informatics.www1.artemis.native_app.core.communication.impl

import de.tum.informatics.www1.artemis.native_app.core.communication.MetisPostAction
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisService
import de.tum.informatics.www1.artemis.native_app.core.datastore.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext

/**
 * Collect updates from the STOMP service and update the database accordingly.
 */
suspend fun updatePosts(
    host: String,
    metisService: MetisService,
    metisStorageService: MetisStorageService,
    context: MetisContext
) {
    metisService.subscribeToPostUpdates(context)
        .collect { dto ->
            when (dto.action) {
                MetisPostAction.CREATE -> {
                    metisStorageService.insertLiveCreatedPost(host, context, dto.post)
                }
                MetisPostAction.UPDATE -> {
                    metisStorageService.updatePost(host, context, dto.post)
                }
                MetisPostAction.DELETE -> {
                    metisStorageService.deletePosts(
                        host,
                        listOf(dto.post.id ?: return@collect)
                    )
                }
                MetisPostAction.READ_CONVERSATION -> {

                }
            }
        }
}