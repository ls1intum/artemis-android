package de.tum.informatics.www1.artemis.native_app.core.data.service.network.impl

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseService
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.CourseAvailableTabs
import de.tum.informatics.www1.artemis.native_app.core.model.CourseExercisesForOverview
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

internal class CourseServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
) : LoggedInBasedServiceImpl(ktorProvider,artemisContextProvider), CourseService {

    override suspend fun getCourse(courseId: Long): NetworkResponse<Course> {
        return getRequest {
            url {
                appendPathSegments(*Api.Course.Courses.path, courseId.toString(), "for-overview")
            }
        }
    }

    override suspend fun getCourseWithContent(courseId: Long): NetworkResponse<Course> =
        coroutineScope {
            val course = async { getCourse(courseId) }
            val availableTabs = async { getAvailableTabs(courseId) }
            val exercises = async { getExercises(courseId) }

            course.await().then { loadedCourse ->
                availableTabs.await().then { tabs ->
                    exercises.await().then { exercisesForOverview ->
                        // The lectures endpoint requires membership in this course specifically,
                        // unlike the three above, and answers an error object rather than a list
                        // when the course has no lecture tab. Asking the tabs first keeps a course
                        // without lectures from failing the whole screen on a response the list
                        // cannot be parsed from.
                        val lectures =
                            if (tabs.lectures) getLectures(courseId) else NetworkResponse.Response(
                                emptyList()
                            )

                        lectures.bind { loadedLectures ->
                            loadedCourse.copy(
                                exercises = exercisesForOverview.exercises,
                                lectures = loadedLectures,
                                faqEnabled = tabs.faq,
                            )
                        }
                    }
                }
            }
        }

    private suspend fun getAvailableTabs(courseId: Long): NetworkResponse<CourseAvailableTabs> {
        return getRequest {
            url {
                appendPathSegments(*Api.Course.Courses.path, courseId.toString(), "available-tabs")
            }
        }
    }

    private suspend fun getExercises(courseId: Long): NetworkResponse<CourseExercisesForOverview> {
        return getRequest {
            url {
                appendPathSegments(
                    *Api.Course.Courses.path,
                    courseId.toString(),
                    "exercises-for-overview"
                )
            }
        }
    }

    private suspend fun getLectures(courseId: Long): NetworkResponse<List<Lecture>> {
        return getRequest {
            url {
                appendPathSegments(
                    *Api.Lecture.path,
                    "courses",
                    courseId.toString(),
                    "lectures-for-overview"
                )
            }
        }
    }
}
