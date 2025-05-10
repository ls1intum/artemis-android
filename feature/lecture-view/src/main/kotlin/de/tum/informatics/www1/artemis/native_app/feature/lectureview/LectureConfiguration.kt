package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class LectureConfiguration(
    val navigationLevel: Int,
    val prev: LectureConfiguration?
) : Parcelable

@Parcelize
object NothingOpened : LectureConfiguration(0, null)

@Parcelize
data class OpenedLecture(
    private val _prev: LectureConfiguration,
    val lectureId: Long
) : LectureConfiguration(10, _prev)
