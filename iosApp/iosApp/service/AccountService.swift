//
// Created by Tim Ortel on 26.09.22.
//

import Foundation
import Combine

/**
 * Service that provides data about the users login status.
 */
protocol AccountService {

    /**
     * The latest authentication data. Publisher emits a new element whenever the login status of the user changes.
     */
    var authenticationData: any Publisher<AuthenticationData, Never> { get }

    func login(username: String, password: String, rememberMe: Bool) async -> LoginResponse

    /**
     - Returns: If the user is currently logged in.
     */
    func isLoggedIn() -> Bool

    /**
     * Deletes the JWT.
     */
    func logout()
}

/**
 * Can either be [LoggedIn] or [NotLoggedIn].
 */
enum AuthenticationData {
    case NotLoggedIn
    case LoggedIn(authToken: String)
}

struct LoginResponse {
    let isSuccessful: Bool
}