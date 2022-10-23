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

        let finalResponse = await performNetworkCall(httpClient: httpClient, maxBodyByteSize: 5 * 1024 * 1024, createRequest: {
            var request = HTTPClientRequest(url: serverUrl + "api/courses/for-dashboard")
            request.method = .GET
            request.headers.add(name: HttpHeaders.Accept, value: ContentTypes.Application.Json)
            request.headers.add(name: HttpHeaders.UserAgent, value: DefaultHttpHeaderValues.ArtemisUserAgent)

            request.headers.add(name: HttpHeaders.Authorization, value: "Bearer " + authorizationToken)
            return request
        }, decode: { body in
            try Dashboard(courses: jsonProvider.decoder.decode([Course].self, from: body))
        })

        try? httpClient.syncShutdown()

        return finalResponse
    }
}
