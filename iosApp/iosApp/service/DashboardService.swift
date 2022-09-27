//
// Created by Tim Ortel on 27.09.22.
//
//

import Foundation

/**
 * Service where you can make requests about the Artemis dashboard.
 */
protocol DashboardService {

    /**
     * Load the dashboard from the specified server using the specified authentication data.
     */
    func loadDashboard(authorizationToken: String, serverUrl: String) async -> NetworkResponse<Dashboard>
}
