package de.tum.informatics.www1.artemis.native_app.core.data

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseService
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.CourseWithScore

class CourseServiceFake(private val course: CourseWithScore) : CourseService {

    constructor(course: Course) : this(
        CourseWithScore(
            course,
            CourseWithScore.TotalScores(
                maxPoints = 0f,
                reachablePoints = 0f,
                studentScores = CourseWithScore.TotalScores.StudentScores(
                    absoluteScore = 0f,
                    relativeScore = 0f,
                    currentRelativeScore = 0f,
                    presentationScore = 0f
                )
            )
        )
    )

    override suspend fun getCourse(
        courseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<CourseWithScore> = NetworkResponse.Response(course)
}
