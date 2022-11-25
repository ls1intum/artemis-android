package de.tum.informatics.www1.artemis.native_app.core.datastore.dao

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface MetisDao {

    @Insert
    fun insert
}