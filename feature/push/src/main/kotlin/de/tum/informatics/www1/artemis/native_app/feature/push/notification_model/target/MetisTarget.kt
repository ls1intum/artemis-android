package de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target

import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.content.MetisContext

sealed interface MetisTarget : NotificationTarget {
    val metisContext: de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.content.MetisContext
    val postId: Long
}