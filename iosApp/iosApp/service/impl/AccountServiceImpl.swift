//
// Created by Tim Ortel on 26.09.22.
// Copyright (c) 2022 orgName. All rights reserved.
//

import Foundation
import Combine
import AsyncHTTPClient
import NIO

class AccountServiceImpl: AccountService {

    let serverCommunicationProvider: ServerCommunicationProvider
    let httpClientProvider: HTTPClientProvider
    let jsonProvider: JsonProvider

    var authenticationData: any Publisher<AuthenticationData, Never> =
            UserDefaults
                    .standard
                    .publisher(for: \.loginJwt)
                    .map { key in
                        if key != nil {
                            return AuthenticationData.NotLoggedIn
                        } else {
                            return AuthenticationData.LoggedIn(authToken: key!)
                        }
                    }

    init(serverCommunicationProvider: ServerCommunicationProvider, httpClientProvider: HTTPClientProvider, jsonProvider: JsonProvider) {
        self.serverCommunicationProvider = serverCommunicationProvider
        self.httpClientProvider = httpClientProvider
        self.jsonProvider = jsonProvider
    }

    func login(username: String, password: String, rememberMe: Bool) async -> LoginResponse {
        let requestBodyData: Data

        do {
            requestBodyData = try jsonProvider.encoder.encode(LoginBody(username: username, password: password, rememberMe: rememberMe))

            var request = HTTPClientRequest(url: serverCommunicationProvider.serverUrl + "api/authenticate")
            request.method = .POST
            request.headers.add(name: "Content-Type", value: "application/json")
            request.body = .bytes(ByteBuffer(data: requestBodyData))

            let client = httpClientProvider.httpClient

            let response = try await client.execute(request, timeout: .seconds(30))

            if (response.status == .ok) {
                response.body
            } else {
                return LoginResponse(isSuccessful: false)
            }
        } catch {
            return LoginResponse(isSuccessful: false)
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