package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnit

data class LectureUnitData(val lectureUnit: LectureUnit, val isUploadingChanges: Boolean)