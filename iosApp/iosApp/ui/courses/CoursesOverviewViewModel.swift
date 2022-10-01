//
// Created by Tim Ortel on 27.09.22.
//

import Foundation
import SwiftUI
import Factory
import Combine
import CombineExt

extension CoursesOverviewView {
    @MainActor class CoursesOverviewViewModel: ObservableObject {

        @Injected(Container.dashboardService) var dashboardService: DashboardService
        @Injected(Container.accountService) var accountService: AccountService
        @Injected(Container.serverCommunicationProvider) var serverCommunicationProvider: ServerCommunicationProvider

        @Published var dashboard: DataState<Dashboard> = DataState.loading
        @Published var bearer: String = ""

        @Published var serverUrl: String = ""

        private let requestReloadDashboard = ReplaySubject<Void, Never>(bufferSize: 1)

        init() {
            accountService
                    .authenticationData
                    .eraseToAnyPublisher()
                    .replaceError(with: AuthenticationData.NotLoggedIn)
                    .receive(on: DispatchQueue.main)
                    .map { authData in
                        switch authData {
                        case .NotLoggedIn:
                            return ""
                        case .LoggedIn(authToken: let authToken):
                            return "Bearer " + authToken
                        }
                    }
                    .assign(to: &$bearer)

            serverCommunicationProvider
                    .serverUrl
                    .assign(to: &$serverUrl)

            let dashboardPublisher: AnyPublisher<DataState<Dashboard>, Never> =
                    accountService
                            .authenticationData
                            .combineLatest(serverCommunicationProvider.serverUrl, requestReloadDashboard.prepend(()))
                            .transformLatest { [self] (continuation, data) in
                                let (authData, serverUrl, _) = data
                                switch authData {
                                case .NotLoggedIn: continuation.send(DataState<Dashboard>.done(response: NetworkResponse<Dashboard>.failure(error: NSError())))
                                case .LoggedIn(authToken: let authToken):
                                    continuation.send(DataState<Dashboard>.loading)

                                    let loadedDashboard = await dashboardService.loadDashboard(authorizationToken: authToken, serverUrl: serverUrl)
                                    continuation.send(DataState.done(response: loadedDashboard))
                                }
                            }


            dashboardPublisher
                    .assign(to: &$dashboard)
        }

        func reloadDashboard() async {
            requestReloadDashboard.send(())
        }

        func logout() {
            accountService.logout()
        }
    }
}
