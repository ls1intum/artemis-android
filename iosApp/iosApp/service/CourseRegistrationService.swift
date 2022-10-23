import Foundation

/**
 * Service that handles all server communication for registering to a course.
 */
protocol CourseRegistrationService {
    /**
     * Fetch the courses the user can register to from the server.
     */
     func fetchRegistrableCourses(serverUrl: String, authToken: String) async -> NetworkResponse<[Course]>

    func registerInCourse(serverUrl: String, authToken: String, courseId: Int) async -> NetworkResponse<Void>
}