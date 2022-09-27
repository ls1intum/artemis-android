//
// Created by Tim Ortel on 27.09.22.
//

import Foundation
import SwiftUI
import Factory

extension CoursesOverviewView {
    @MainActor class CoursesOverviewViewModel: ObservableObject {

        @Injected(Container.dashboardService) var dashboardService: DashboardService
        @Injected(Container.accountService) var accountService: AccountService
        @Injected(Container.serverCommunicationProvider) var serverCommunicationProvider: ServerCommunicationProvider

        @Published var dashboard: DataState<Dashboard> = DataState.loading
        @Published var bearer: String = ""

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
        }

        /**
         * TODO: automatically reload dashboard when login status changes, the server url changes etc.
         */
        func reloadDashboard() async {
            let authData = await accountService.authenticationData.eraseToAnyPublisher().values.first(where: { _ in true })!

            switch authData {
            case .NotLoggedIn: return
            case .LoggedIn(authToken: let authToken):
                dashboard = DataState.loading
                let loadedDashboard = await dashboardService.loadDashboard(authorizationToken: authToken, serverUrl: serverCommunicationProvider.serverUrl)
                dashboard = DataState.done(response: loadedDashboard)
            }
        }

        func logout() {
            accountService.logout()
        }
    }
}
