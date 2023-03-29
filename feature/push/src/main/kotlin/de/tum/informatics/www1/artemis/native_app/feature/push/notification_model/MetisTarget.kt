package de.tum.informatics.www1.artemis.native_app.feature.push.notification_model

import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext

sealed interface MetisTarget : NotificationTarget {
    val metisContext: MetisContext
    val postId: Long
}