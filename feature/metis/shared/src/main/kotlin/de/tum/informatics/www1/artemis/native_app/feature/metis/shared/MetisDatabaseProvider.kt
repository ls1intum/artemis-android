package de.tum.informatics.www1.artemis.native_app.feature.metis.shared

import de.tum.informatics.www1.artemis.native_app.core.datastore.room.BaseDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.MetisDao

interface MetisDatabaseProvider : BaseDatabaseProvider {
    val metisDao: MetisDao
}