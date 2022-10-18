package de.tum.informatics.www1.artemis.native_app.android.util.parceler

import android.os.Parcel
import kotlinx.datetime.Instant
import kotlinx.parcelize.Parceler

object InstantParceler : Parceler<Instant?> {
    override fun create(parcel: Parcel): Instant? {
        if (!parcel.readBoolean()) return null
        val epochSeconds = parcel.readLong()
        val nanosecondsOfSecond = parcel.readInt()

        return Instant.fromEpochSeconds(epochSeconds, nanosecondsOfSecond)
    }

    override fun Instant?.write(parcel: Parcel, flags: Int) {
        parcel.writeBoolean(this != null)
        if (this != null) {
            parcel.writeLong(epochSeconds)
            parcel.writeInt(nanosecondsOfSecond)
        }
    }
}