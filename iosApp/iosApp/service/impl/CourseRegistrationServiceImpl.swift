import Foundation
import AsyncHTTPClient

class CourseRegistrationServiceImpl: CourseRegistrationService {

    private let jsonProvider: JsonProvider

    init(jsonProvider: JsonProvider) {
        self.jsonProvider = jsonProvider
    }

    func fetchRegistrableCourses(serverUrl: String, authToken: String) async -> NetworkResponse<[Course]> {
        await HTTPClient(eventLoopGroupProvider: .createNew).use { httpClient in
            await performNetworkCall(httpClient: httpClient, maxBodyByteSize: 30.MB(), createRequest: {
                var request = HTTPClientRequest(url: serverUrl + "api/courses/for-registration")
                request.method = .GET

                request.headers.add(name: HttpHeaders.Accept, value: ContentTypes.Application.Json)
                request.headers.add(name: HttpHeaders.UserAgent, value: DefaultHttpHeaderValues.ArtemisUserAgent)
                request.headers.add(name: HttpHeaders.Authorization, value: "Bearer " + authToken)

                return request
            }, decode: { body in
                try jsonProvider.decoder.decode([Course].self, from: body)
            })
        }
    }

    func registerInCourse(serverUrl: String, authToken: String, courseId: Int) async -> NetworkResponse<()> {
        await HTTPClient(eventLoopGroupProvider: .createNew).use { httpClient in
            await performNetworkCall(httpClient: httpClient, maxBodyByteSize: 30.MB(), createRequest: {
                var request = HTTPClientRequest(url: serverUrl + "api/" + String(courseId) + "/register")
                request.method = .POST

                request.headers.add(name: HttpHeaders.UserAgent, value: DefaultHttpHeaderValues.ArtemisUserAgent)
                request.headers.add(name: HttpHeaders.Authorization, value: "Bearer " + authToken)

                return request
            }, decode: { body in
                try jsonProvider.decoder.decode(Account.self, from: body)
            })
                    .bind(f: { () })
        }
    }
}