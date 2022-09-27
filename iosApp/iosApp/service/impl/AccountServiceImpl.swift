//
// Created by Tim Ortel on 26.09.22.
//

import Foundation
import Combine
import AsyncHTTPClient
import NIO

class AccountServiceImpl: AccountService {

    let serverCommunicationProvider: ServerCommunicationProvider
    let jsonProvider: JsonProvider

    /**
     * Only set if the user logged in without remember me.
     */
    private let inMemoryJWT: CurrentValueSubject<String?, Never>

    let authenticationData: any Publisher<AuthenticationData, Never>

    init(serverCommunicationProvider: ServerCommunicationProvider, jsonProvider: JsonProvider) {
        self.serverCommunicationProvider = serverCommunicationProvider
        self.jsonProvider = jsonProvider

        inMemoryJWT = CurrentValueSubject(nil)

        authenticationData =
//                Publishers.Zip(
//                                UserDefaults
//                                        .standard
//                                        .publisher(for: \.loginJwt),
//                                inMemoryJWT
//                        )
//                        .map { storedJWT, inMemoryJWT in
//                            inMemoryJWT ?? storedJWT
//                        }
                UserDefaults
                        .standard
                        .publisher(for: \.loginJwt)
                        .map { key in
                            if let setKey = key {
                                return AuthenticationData.LoggedIn(authToken: setKey)
                            } else {
                                return AuthenticationData.NotLoggedIn
                            }
                        }
    }

    func login(username: String, password: String, rememberMe: Bool) async -> LoginResponse {
        let client = HTTPClient(eventLoopGroupProvider: .createNew)

        do {
            let requestBodyData = try jsonProvider.encoder.encode(LoginBody(username: username, password: password, rememberMe: rememberMe))

            var request = HTTPClientRequest(url: serverCommunicationProvider.serverUrl + "api/authenticate")
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