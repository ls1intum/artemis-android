//
// Created by Tim Ortel on 26.09.22.
//
//

import Foundation
import Combine

protocol AccountService {

    var authenticationData: any Publisher<AuthenticationData, Never> { get }

    func login(username: String, password: String, rememberMe: Bool) async -> LoginResponse
}

enum AuthenticationData {
    case NotLoggedIn
    case LoggedIn(authToken: String)
}

struct LoginResponse {
    let isSuccessful: Bool
}