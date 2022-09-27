//
// Created by Tim Ortel on 27.09.22.
//

import Foundation
import AsyncHTTPClient

/**
 * DashboardService implementation that requests the dashboard from the Artemis server.
 */
class DashboardServiceImpl: DashboardService {

    let jsonProvider: JsonProvider

    init(jsonProvider: JsonProvider) {
        self.jsonProvider = jsonProvider
    }

    func loadDashboard(authorizationToken: String, serverUrl: String) async -> NetworkResponse<Dashboard> {
        let httpClient = HTTPClient(eventLoopGroupProvider: .createNew)

        let finalResponse: NetworkResponse<Dashboard>
        do {
            var request = HTTPClientRequest(url: serverUrl + "api/courses/for-dashboard")
            request.method = .GET
            request.headers.add(name: "accept", value: "application/json")
            request.headers.add(name: "User-Agent", value: "artemis-native-client")

            request.headers.add(name: "Authorization", value: "Bearer " + authorizationToken)

            let response = try await httpClient.execute(request, timeout: .seconds(30))

            if (response.status == .ok) {
                let body = try await response.body.collect(upTo: 5 * 1024 * 1024) // 5MB

                let dashboardResponse: [Course] = try jsonProvider.decoder.decode([Course].self, from: body)
                finalResponse = NetworkResponse.response(data: Dashboard(courses: dashboardResponse))
            } else {
                finalResponse = NetworkResponse.failure(error: NSError(domain: "", code: Int(response.status.code)))
            }
        } catch let e {
            finalResponse = NetworkResponse.failure(error: e)
        }

        try? httpClient.syncShutdown()

        return finalResponse
    }
}
