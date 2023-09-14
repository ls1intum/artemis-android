package de.tum.informatics.www1.artemis.native_app.feature.lectureview.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture

interface LectureService {

    suspend fun loadLecture(
        lectureId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Lecture>

    suspend fun completeLectureUnit(
        lectureUnitId: Long,
        lectureId: Long,
        completed: Boolean,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Unit>
}