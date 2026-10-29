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
                // Only the course itself decides whether this screen can be shown. The rest is
                // content: a course that answers but whose exercises momentarily do not is still
                // worth rendering, and failing the whole screen would also take down everything
                // else derived from it.
                val tabs = availableTabs.await().or(CourseAvailableTabs())

                // The lectures endpoint requires membership in this course specifically, unlike the
                // other three, so it is only asked when the tabs say there are lectures to show.
                val lectures =
                    if (tabs.lectures) getLectures(courseId).or(emptyList()) else emptyList()

                NetworkResponse.Response(
                    loadedCourse.copy(
                        exercises = exercises.await().or(CourseExercisesForOverview()).exercises,
                        lectures = lectures,
                        faqEnabled = tabs.faq,
                    )
                )
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
