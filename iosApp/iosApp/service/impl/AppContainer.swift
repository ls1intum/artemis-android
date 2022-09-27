//
// Created by Tim Ortel on 27.09.22.
//
//

import Factory

/**
 * Dependency injection container for this app.
 */
extension Container {
    static let serverCommunicationProvider = Factory<ServerCommunicationProvider> {
        ServerCommunicationProviderImpl()
    }

    static let jsonProvider = Factory<JsonProvider> {
        JsonProvider()
    }

    static let accountService = Factory<AccountService>(scope: .singleton) {
        AccountServiceImpl(serverCommunicationProvider: serverCommunicationProvider(), jsonProvider: JsonProvider())
    }

    static let dashboardService = Factory<DashboardService> {
        DashboardServiceImpl(jsonProvider: jsonProvider())
    }
}