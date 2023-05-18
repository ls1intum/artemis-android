package de.tum.informatics.www1.artemis.native_app.feature.metis

import de.tum.informatics.www1.artemis.native_app.core.datastore.room.BaseDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.dao.MetisDao

interface MetisDatabaseProvider : BaseDatabaseProvider {
    val metisDao: MetisDao
}