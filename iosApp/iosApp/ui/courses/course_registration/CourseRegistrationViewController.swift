import Foundation
import SwiftUI
import Factory
import Combine

@MainActor class CourseRegistrationViewController: ObservableObject {

    private let accountService = Container.accountService()
    private let networkStatusProvider = Container.networkStatusProvider()
    private let serverCommunicationProvider = Container.serverCommunicationProvider()
    private let courseRegistrationService = Container.courseRegistrationService()

    @Published var registrableCourses: DataState<[SemesterCourses]> = .loading

    private let reloadRegistrableCoursesSubject = PassthroughSubject<Void, Never>()

    init() {
        let registrableCoursesPublisher: AnyPublisher<DataState<[SemesterCourses]>, Never> =
                accountService.authenticationData
                        .combineLatest(serverCommunicationProvider.serverUrl, reloadRegistrableCoursesSubject.prepend(()))
                        .transformLatest { [self] sub, data in
                            let (authData, serverUrl, _) = data

                            switch authData {
                            case .LoggedIn(let authToken, _):
                                do {
                                    try await sub.sendAll(
                                            publisher: retryOnInternet(connectivity: networkStatusProvider.currentNetworkStatus) { [self] in
                                                await courseRegistrationService.fetchRegistrableCourses(serverUrl: serverUrl, authToken: authToken)
                                            }
                                    )
                                } catch {

                                }
                            case .NotLoggedIn: sub.send(DataState.suspended(error: nil))
                            }
                        }
                        .map { (dataState: DataState<[Course]>) in
                            dataState.bind(op: { courses in
                                Dictionary(grouping: courses, by: { $0.semester ?? "" })
                                        .map { semester, courses in
                                            SemesterCourses(semester: semester, courses: courses)
                                        }
                            })
                        }
                        .eraseToAnyPublisher()

        registrableCoursesPublisher
                .receive(on: DispatchQueue.main)
                .assign(to: &$registrableCourses)
    }

    func reloadRegistrableCourses() {
        reloadRegistrableCoursesSubject.send(())
    }
}

struct SemesterCourses: Identifiable {
    let semester: String
    let courses: [Course]

    var id: Int {
        get {
            semester.hash
        }
    }
}