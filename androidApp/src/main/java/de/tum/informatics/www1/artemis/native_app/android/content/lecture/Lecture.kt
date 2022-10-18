package de.tum.informatics.www1.artemis.native_app.android.content.lecture

import android.os.Parcelable
import de.tum.informatics.www1.artemis.native_app.android.content.lecture.attachment.Attachment
import de.tum.informatics.www1.artemis.native_app.android.util.parceler.InstantParceler
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith

@Serializable
@Parcelize
data class Lecture(
    val id: Int,
    val title: String,
    val description: String? = null,
    val startDate: @WriteWith<InstantParceler> Instant? = null,
    val endDate: @WriteWith<InstantParceler> Instant? = null,
    val attachments: List<Attachment> = emptyList()
) : Parcelable