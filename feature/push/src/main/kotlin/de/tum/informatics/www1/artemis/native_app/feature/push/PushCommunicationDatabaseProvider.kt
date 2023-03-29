package de.tum.informatics.www1.artemis.native_app.feature.push

import de.tum.informatics.www1.artemis.native_app.core.datastore.room.BaseDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model.PushCommunicationDao

interface PushCommunicationDatabaseProvider : BaseDatabaseProvider {
    val pushCommunicationDao: PushCommunicationDao
}