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
        let networkStatusProvider: NetworkStatusProvider = Container.networkStatusProvider()

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
                        case .LoggedIn(authToken: let authToken, _):
                            return "Bearer " + authToken
                        }
                    }
                    .assign(to: &$bearer)

            serverCommunicationProvider
                    .serverUrl
                    .receive(on: DispatchQueue.main)
                    .assign(to: &$serverUrl)

            let dashboardPublisher: AnyPublisher<DataState<Dashboard>, Never> =
                    accountService
                            .authenticationData
                            .combineLatest(serverCommunicationProvider.serverUrl, requestReloadDashboard.prepend(()))
                            .transformLatest { [self] (continuation, data) in
                                let (authData, serverUrl, _) = data
                                switch authData {
                                case .LoggedIn(authToken: let authToken, _):
                                    try? await continuation.sendAll(publisher:
                                        retryOnInternet(connectivity: networkStatusProvider.currentNetworkStatus) { [self] in
                                            await dashboardService.loadDashboard(authorizationToken: authToken, serverUrl: serverUrl)
                                        }
                                    )
                                case .NotLoggedIn: continuation.send(DataState<Dashboard>.suspended(error: nil))
                                }
                            }


            dashboardPublisher
                    .receive(on: DispatchQueue.main)
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
