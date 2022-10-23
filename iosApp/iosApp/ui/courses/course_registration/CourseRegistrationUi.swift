import Foundation
import SwiftUI
import Factory
import MarkdownUI

struct CourseRegistrationView: View {

    private let accountService = Container.accountService()
    private let serverCommunicationProvider = Container.serverCommunicationProvider()

    @StateObject var viewModel = CourseRegistrationViewController()

    @ObservedObject var bearer: ObservedValue<String>
    @ObservedObject var serverUrl: ObservedValue<String?>

    /**
     * If the user clicks on signup, this variable holds the course the user wants to sign up to. While set, a dialog with the registration information is displayed.
     */
    @State var courseCandidate: Course? = nil

    init() {
        bearer = ObservedValue(
                publisher: accountService.authenticationData.map { authData in
                            switch authData {
                            case .NotLoggedIn: return ""
                            case .LoggedIn(authToken: let authToken, _): return "Bearer " + authToken
                            }
                        }
                        .eraseToAnyPublisher(),
                initialValue: "")
        serverUrl = ObservedValue<String?>(publisher: serverCommunicationProvider.serverUrl.map {
                    $0
                }
                .eraseToAnyPublisher(), initialValue: nil)
    }

    var body: some View {
        let properServerUrl = serverUrl.latestValue
        if properServerUrl == nil {
            EmptyView()
        } else {
            RegisterForCourseContentView(
                    courses: viewModel.registrableCourses,
                    reloadCourses: {
                        viewModel.reloadRegistrableCourses()
                    },
                    serverUrl: properServerUrl!,
                    bearerToken: bearer.latestValue,
                    onClickSignUp: { course in courseCandidate = course }
            )
                    .navigationTitle("course_registration_title")
                    .sheet(item: $courseCandidate, content: { selectedCourse in
                        CourseRegistrationSheetView(course: selectedCourse)
                    }
                    )
        }
    }
}

struct RegisterForCourseContentView: View {

    private let courses: DataState<[SemesterCourses]>
    private let reloadCourses: () -> Void
    private let serverUrl: String
    private let bearerToken: String
    private let onClickSignUp: (Course) -> Void

    init(courses: DataState<[SemesterCourses]>, reloadCourses: @escaping () -> (), serverUrl: String, bearerToken: String, onClickSignUp: @escaping (Course) -> Void) {
        self.courses = courses
        self.reloadCourses = reloadCourses
        self.serverUrl = serverUrl
        self.bearerToken = bearerToken
        self.onClickSignUp = onClickSignUp
    }

    var body: some View {
        BasicDataStateView(
                data: courses,
                loadingText: "course_registration_loading_courses_loading",
                failureText: "course_registration_loading_courses_failed",
                suspendedText: "course_registration_loading_courses_suspended",
                retryButtonText: "course_registration_loading_courses_try_again",
                clickRetryButtonAction: reloadCourses
        ) { data in
            ScrollView {
                LazyVStack(spacing: 8) {
                    ForEach(data) { semesterCourse in
                        Section(header: Text(verbatim: semesterCourse.semester)) {
                            ForEach(semesterCourse.courses, id: \.self.id) { course in
                                RegistrableCourseView(
                                        course: course,
                                        serverUrl: serverUrl,
                                        bearerToken: bearerToken,
                                        onClickSignup: { onClickSignUp(course) }
                                )
                                        .padding(.horizontal, 16)
                            }
                        }
                    }
                }
            }
        }
    }
}

private struct RegistrableCourseView: View {

    private let course: Course
    private let serverUrl: String
    private let bearerToken: String
    private let onClickSignup: () -> Void

    init(course: Course, serverUrl: String, bearerToken: String, onClickSignup: @escaping () -> Void) {
        self.course = course
        self.serverUrl = serverUrl
        self.bearerToken = bearerToken
        self.onClickSignup = onClickSignup
    }

    var body: some View {
        CoursesHeaderView(course: course, serverUrl: serverUrl, bearer: bearerToken) {
            VStack(spacing: 0) {
                Divider()

                HStack {
                    Spacer()

                    Button(
                            action: onClickSignup,
                            label: { Text("course_registration_sign_up") }
                    )
                            .padding(.bottom, 8)
                            .padding(.trailing, 8)
                            .buttonStyle(.borderedProminent)
                }
                        .padding(.top, 8)
            }
        }
    }
}

private struct CourseRegistrationSheetView: View {

    let course: Course

    var body: some View {
        let registrationConfirmationMessage = course.registrationConfirmationMessage

        VStack {
            ScrollView {
                if registrationConfirmationMessage != nil {
                    Markdown(registrationConfirmationMessage ?? "")
                            .padding(.horizontal, 16)
                            .padding(.vertical, 32)
                } else {
                    VStack {
                        Text(verbatim: course.title ?? "")
                        Text("course_registration_sign_up_dialog_message")
                    }
                            .padding(.horizontal, 16)
                            .padding(.vertical, 32)
                }

            }
        }

        Spacer()

        Button(action: {}, label: { Text("course_registration_sign_up_dialog_positive_button") })
                .buttonStyle(.borderedProminent)
                .padding(.bottom, 32)
                .presentationDetents([.medium, .large])
    }
}