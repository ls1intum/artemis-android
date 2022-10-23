//
// Created by Tim Ortel on 26.09.22.
//

import Foundation
import Combine
import AsyncHTTPClient
import NIO

class AccountServiceImpl: AccountService {

    let serverCommunicationProvider: ServerCommunicationProvider
    let networkStatusProvider: NetworkStatusProvider
    let jsonProvider: JsonProvider

    /**
     * Only set if the user logged in without remember me.
     */
    private let inMemoryJWT: CurrentValueSubject<String?, Never>

    let authenticationData: AnyPublisher<AuthenticationData, Never>

    init(serverCommunicationProvider: ServerCommunicationProvider, jsonProvider: JsonProvider, networkStatusProvider: NetworkStatusProvider) {
        self.serverCommunicationProvider = serverCommunicationProvider
        self.jsonProvider = jsonProvider
        self.networkStatusProvider = networkStatusProvider

        inMemoryJWT = CurrentValueSubject(nil)

        authenticationData =
                UserDefaults
                        .standard
                        .publisher(for: \.loginJwt)
                        .combineLatest(inMemoryJWT)
                        .map { storedJWT, inMemoryJWT in
                            inMemoryJWT ?? storedJWT
                        }
                        .transformLatest { sub, key in
                            //TODO: Verify if the key has expired, etc
                            if let setKey = key {
                                try! await sub.sendAll(
                                        publisher: serverCommunicationProvider
                                                .serverUrl
                                                .transformLatest { sub2, serverUrl in
                                                    try! await sub2.sendAll(
                                                            publisher: retryOnInternet(connectivity: networkStatusProvider.currentNetworkStatus) {
                                                                await AccountServiceImpl.getAccountData(bearer: "Bearer " + setKey, serverUrl: serverUrl, jsonProvider: jsonProvider)
                                                            }
                                                    )
                                                }
                                                .map { account in
                                                    AuthenticationData.LoggedIn(authToken: setKey, account: account)
                                                }
                                                .eraseToAnyPublisher()
                                )
                            } else {
                                sub.send(AuthenticationData.NotLoggedIn)
                            }
                        }
                        .share(replay: 1)
                        .eraseToAnyPublisher()
    }

    func login(username: String, password: String, rememberMe: Bool) async -> LoginResponse {
        let client = HTTPClient(eventLoopGroupProvider: .createNew)
        let serverUrl = await serverCommunicationProvider.serverUrl.first().values.first(where: { _ in true })!

        do {
            let requestBodyData = try jsonProvider.encoder.encode(LoginBody(username: username, password: password, rememberMe: rememberMe))

            var request = HTTPClientRequest(url: serverUrl + "api/authenticate")
            request.method = .POST
            request.headers.add(name: "content-type", value: "application/json")
            request.headers.add(name: "accept", value: "application/json")
            request.headers.add(name: "User-Agent", value: "artemis-native-client")
            request.body = .bytes(ByteBuffer(data: requestBodyData))

            let response = try await client.execute(request, timeout: .seconds(30))

            if (response.status == .ok) {
                let body = try await response.body.collect(upTo: 5 * 1024) // 5KB
                let loginResponse = try jsonProvider.decoder.decode(LoginResponseBody.self, from: body)

                //either store the token permanently, or just cache it in memory.
                if (rememberMe) {
                    UserDefaults.standard.loginJwt = loginResponse.id_token
                } else {
                    inMemoryJWT.send(loginResponse.id_token)
                }

                try client.syncShutdown()
                return LoginResponse(isSuccessful: true)
            } else {
                let body = try await response.body.collect(upTo: 5 * 1024)

                let problem = String(buffer: body)
                print(problem)

                try client.syncShutdown()
                return LoginResponse(isSuccessful: false)
            }
        } catch {
            try? client.syncShutdown()
            return LoginResponse(isSuccessful: false)
        }
    }

    func isLoggedIn() -> Bool {
        inMemoryJWT.value != nil || UserDefaults.standard.loginJwt != nil
    }

    func logout() {
        inMemoryJWT.value = nil
        UserDefaults.standard.loginJwt = nil
    }

    private static func getAccountData(bearer: String, serverUrl: String, jsonProvider: JsonProvider) async -> NetworkResponse<Account> {
        await HTTPClient(eventLoopGroupProvider: .createNew).use { httpClient in
            await performNetworkCall(httpClient: httpClient, createRequest: {
                var request = HTTPClientRequest(url: serverUrl + "api/account")
                request.headers.add(name: HttpHeaders.Accept, value: ContentTypes.Application.Json)
                request.headers.add(name: HttpHeaders.UserAgent, value: DefaultHttpHeaderValues.ArtemisUserAgent)
                request.headers.add(name: HttpHeaders.Authorization, value: bearer)

                return request
            }, decode: { body in try jsonProvider.decoder.decode(Account.self, from: body) })
        }
    }
}

extension UserDefaults {
    @objc var loginJwt: String? {
        get {
            string(forKey: "jwt")
        }
        set {
            set(newValue, forKey: "jwt")
        }
    }
}

private struct LoginBody: Codable {
    let username: String
    let password: String
    let rememberMe: Bool
}

private struct LoginResponseBody: Codable {
    let id_token: String
}